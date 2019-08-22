package com.xuecheng.manage_media.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.RandomAccess;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.domain.media.response.MediaCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_media.config.RabbitMQConfig;
import com.xuecheng.manage_media.dao.MediaFileRepository;

@Service
public class MediaUploadService {

	@Autowired
	private MediaFileRepository mediaFileRepository;
	@Autowired
	private RabbitTemplate rabbitTemplate;
	
	@Value("${xc-service-manage-media.upload-location}")
	private String uploadPath;
	@Value("${xc-service-manage-media.mq.routingkey-media-video}")
	private String routingkey_media_video;
	
	//文件上传注册
	public ResponseResult register(String fileMd5, String fileName, String mimetype, String fileExt, Long fileSize) {
		//检查文件是否上传
		//1.得到文件的路径
		String filePath = getFilePath(fileMd5, fileExt);
		File file = new File(filePath);

		//2.查询数据库文件是否存在
		Optional<MediaFile> optional = mediaFileRepository.findById(fileMd5);
		//文件存在直接返回
		if(file.exists() && optional.isPresent()) {
			ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGISTER_EXIST);
		}
		
		//文件不存在时做一些准备工作，检查文件所在目录是否存在，如果不存在则创建
		boolean fileFold = createFileFold(fileMd5);
		if(!fileFold) {
			//上传文件目录创建失败
			ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGISTER_FAIL);
		}
		
		return new ResponseResult(CommonCode.SUCCESS);
	}
	
	//检查块文件
	public CheckChunkResult checkchunk(String fileMd5, Integer chunk, Integer chunkSize) {
		//得到文件所在的路径
		String chunkfileFolderPath = getChunkFileFolderPath(fileMd5);
		//块文件的文件名称以1,2,3..序号命名，没有扩展名
		File chunkFile = new File(chunkfileFolderPath + chunk);
		if(chunkFile.exists()) {
			return new CheckChunkResult(CommonCode.SUCCESS,true);
		}else {
			return new CheckChunkResult(CommonCode.SUCCESS,false); 
		}
	}
	
	//块文件上传
	public ResponseResult uploadchunk(MultipartFile file, Integer chunk, String fileMd5) {
		//创建块文件目录
		createChunkFileFolder(fileMd5);
		//块文件
		File chunkfile = new File(getChunkFileFolderPath(fileMd5) + chunk);
		//上传的块文件
		InputStream inputStream = null;
		FileOutputStream fileOutputStream = null;
		try {
			inputStream = file.getInputStream();
			fileOutputStream = new FileOutputStream(chunkfile);
			IOUtils.copy(inputStream, fileOutputStream);
		} catch (Exception e) {
			e.printStackTrace();
			ExceptionCast.cast(MediaCode.CHUNK_FILE_EXIST_CHECK);
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				fileOutputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return new ResponseResult(CommonCode.SUCCESS);
	}
	
	
	//合并所有块文件
	public ResponseResult mergechunks(String fileMd5, String fileName, String mimetype, String fileExt, Long fileSize) {
		//获取块文件所属目录
		String chunkfileFolderPath = getChunkFileFolderPath(fileMd5);
		File chunkfileFolder = new File(chunkfileFolderPath);
		if(!chunkfileFolder.exists()) {
			chunkfileFolder.mkdirs();
		}
		//创建合并文件
		File mergeFile = new File(getFilePath(fileMd5, fileExt));
		if(mergeFile.exists()) {
			mergeFile.delete();
		}else {
			try {
				mergeFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		//获取块文件
		List<File> chunkFiles = getChunkFiles(chunkfileFolder);
		
		//执行合并文件
		mergeFile = mergeFile(mergeFile, chunkFiles);
		
		if(mergeFile == null) {
			ExceptionCast.cast(MediaCode.MERGE_FILE_FAIL);
		}
		
		//校验文件MD5
		boolean checkResult = checkFileMd5(mergeFile, fileMd5);
		if(!checkResult) {
			ExceptionCast.cast(MediaCode.MERGE_FILE_CHECKFAIL);
		}
		
		//将文件信息保存到数据库
		MediaFile mediaFile = new MediaFile();
		mediaFile.setFileId(fileMd5);
		mediaFile.setFileName(fileMd5 + "." + fileExt);
		mediaFile.setFileOriginalName(fileName);
		//文件路径保存相对路径
		mediaFile.setFilePath(getFileFolderRelativePath(fileMd5,fileExt));
		mediaFile.setFileSize(fileSize);
		mediaFile.setUploadTime(new Date());
		mediaFile.setMimeType(mimetype);
		mediaFile.setFileType(fileExt);
		//状态为上传成功
		mediaFile.setFileStatus("301002");
		mediaFileRepository.save(mediaFile);
		
		//向Rabbitmq发送信息
		ResponseResult responseResult = sendProcessVideoMsg(mediaFile.getFileId());
		if(responseResult.getCode() != 10000) {
			ExceptionCast.cast(MediaCode.SEND_PROCESS_VIDEO_MSG_FAIL);
		}
		
		return new ResponseResult(CommonCode.SUCCESS);
	}
	
	/** 
	* 根据文件md5得到文件路径
	* 规则：
	* 一级目录：md5的第一个字符
	* 二级目录：md5的第二个字符
	* 三级目录：md5
	* 文件名：md5+文件扩展名
	* @param fileMd5 文件md5值
	* @param fileExt 文件扩展名
	* @return 文件路径
	*/
	private String getFilePath(String fileMd5,String fileExt) {
		String filePath = uploadPath + fileMd5.substring(0, 1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/" + fileMd5 + "." + fileExt;
		return filePath;
	}
	
	//得到文件目录相对路径，路径中去掉根目录
	private String getFileFolderRelativePath(String fileMd5,String fileExt) {
		String filePath = fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/";
		return filePath;
	}
	
	//得到文件所在目录
	private String getFileFolderPath(String fileMd5) {
		String fileFolderPath = uploadPath+ fileMd5.substring(0, 1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/" ;
		return fileFolderPath;
	}
	
	//创建文件目录
	private boolean createFileFold(String fileMd5) {
		//创建上传文件目录
		String fileFolderPath = getFileFolderPath(fileMd5);
		File fileFolder = new File(fileFolderPath);
		if(!fileFolder.exists()) {
			//创建文件夹
			boolean mkdirs = fileFolder.mkdirs();
			return mkdirs;
		}
		return true;
	}
	
	//得到块文件所在目录
	private String getChunkFileFolderPath(String fileMd5) {
		String fileChunkFolderPath = getFileFolderPath(fileMd5) + "chunks" + "/";
		return fileChunkFolderPath;
	}
	
	//创建块文件目录
	private boolean createChunkFileFolder(String fileMd5) {
		//创建上传文件目录
		String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
		File chunkFileFolder = new File(chunkFileFolderPath);
		if(!chunkFileFolder.exists()) {
			//创建文件夹
			boolean mkdirs = chunkFileFolder.mkdirs();
			return mkdirs;
		}
		return true;
	}
	
	//检验文件的MD5值
	private boolean checkFileMd5(File mergeFile,String md5) {
		//进行Md5校验
		FileInputStream mergeFileInputstream = null;
		try {
			mergeFileInputstream = new FileInputStream(mergeFile);
			//得到文件的Md5
			String mergeFileMd5 = DigestUtils.md5Hex(mergeFileInputstream);
			//比较Md5
			if(md5.equalsIgnoreCase(mergeFileMd5)) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}
	
	//获取所有块文件
	private List<File> getChunkFiles(File chunkfileFolder){
		//获取路径下的所有块文件
		File[] chunkFiles = chunkfileFolder.listFiles();
		//将文件数组转成list，并排序
		List<File> chunkFileList = new ArrayList<>();
		chunkFileList.addAll(Arrays.asList(chunkFiles));
		//排序
		Collections.sort(chunkFileList,new Comparator<File>() {
			@Override
			public int compare(File file1, File file2) {
				if(Integer.parseInt(file1.getName()) > Integer.parseInt(file2.getName())) {
					return 1;
				}
				return -1;
			}
		});
		
		return chunkFileList;
	}
	
	//合并文件
	private File mergeFile(File mergeFile,List<File> chunkFiles) {
		try {
			//创建写文件对象
			RandomAccessFile raf_write = new RandomAccessFile(mergeFile, "rw");
			//遍历分块文件开始合并
			//读取文件缓冲区
			byte[] buff = new byte[1024];
			for(File chunkFile : chunkFiles) {
				RandomAccessFile raf_read = new RandomAccessFile(chunkFile, "r");
				int len = -1;
				//读取分块文件
				while( (len = raf_read.read(buff)) != -1) {
					//向合并文件中写数据
					raf_write.write(buff, 0, len);
				}
				raf_read.close();
			}
			raf_write.close();
			return mergeFile;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private ResponseResult sendProcessVideoMsg(String mediaId) {
		//判断数据库中是否存在该文件
		Optional<MediaFile> optional = mediaFileRepository.findById(mediaId);
		if(!optional.isPresent()) {
			ExceptionCast.cast(MediaCode.SEND_PROCESS_VIDEO_MSG_FAIL);
		}
		MediaFile mediaFile = optional.get();
		//发送视频处理消息
		Map<String, String> msgMap = new HashMap<>();
		msgMap.put("mediaId", mediaId);
		//发送的消息
		String msg = JSON.toJSONString(msgMap);
		rabbitTemplate.convertAndSend(RabbitMQConfig.EX_MEDIA_PROCESSTASK,routingkey_media_video,msg);
		
		return new ResponseResult(CommonCode.SUCCESS);
	}
}
