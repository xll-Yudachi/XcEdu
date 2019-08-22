package com.xuecheng.file_system.service;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient1;
import org.csource.fastdfs.StorageServer;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.xuecheng.file_system.dao.FileSystemRepository;
import com.xuecheng.framework.domain.filesystem.FileSystem;
import com.xuecheng.framework.domain.filesystem.response.FileSystemCode;
import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;

@Service
public class FileSystemService {

	@Value("${xuecheng.fastdfs.tracker_servers}")
	private String tracker_servers;
	@Value("${xuecheng.fastdfs.connect_timeout_in_seconds}")
	private int connect_timeout_in_seconds;
	@Value("${xuecheng.fastdfs.network_timeout_in_seconds}")
	private int network_timeout_in_seconds;
	@Value("${xuecheng.fastdfs.charset}")
	private String charset;

	@Autowired
	private FileSystemRepository fileSystemRepository;

	// 上传文件
	public UploadFileResult upload(MultipartFile multipartFile,
		String filetag,String businesskey,String metadata) {
		if(multipartFile == null) {
			ExceptionCast.cast(FileSystemCode.FS_UPLOADFILE_FILEISNULL);
		}
		
		//上传文件到FastDFS
		String fileId = fdfs_upload(multipartFile);
		if(StringUtils.isEmpty(fileId)) {
			ExceptionCast.cast(FileSystemCode.FS_UPLOADFILE_SERVERFAIL);
		}
		
		//创建文件信息对象
		FileSystem fileSystem = new FileSystem();
		//文件Id
		fileSystem.setFileId(fileId);
		//文件在文件系统中的路径
		fileSystem.setFilePath(fileId);
		//业务标识
		fileSystem.setBusinesskey(businesskey);
		//标签
		fileSystem.setFiletag(filetag);
		//元数据
		if(StringUtils.isNotEmpty(metadata)) {
			try {
				Map map = JSON.parseObject(metadata,Map.class);
				fileSystem.setMetadata(map);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		//名称
		fileSystem.setFileName(multipartFile.getOriginalFilename());
		//大小
		fileSystem.setFileSize(multipartFile.getSize());
		//文件类型
		fileSystem.setFileType(multipartFile.getContentType());
		
		fileSystemRepository.save(fileSystem);
		
		return new UploadFileResult(CommonCode.SUCCESS, fileSystem);
	}

	// 初始化FastDFS的配置
	private void initFastDFSConfig() {
		try {
			ClientGlobal.initByTrackers(tracker_servers);
			ClientGlobal.setG_connect_timeout(connect_timeout_in_seconds);
			ClientGlobal.setG_network_timeout(network_timeout_in_seconds);
			ClientGlobal.setG_charset(charset);
		} catch (Exception e) {
			e.printStackTrace();
			//初始化文件系统出错
			ExceptionCast.cast(FileSystemCode.FS_INITFDFSERROR);
		}
	}

	//上传文件到FastDFS,返回文件ID
	public String fdfs_upload(MultipartFile multipartFile) {
		try {
			//加载FastDFS的配置
			initFastDFSConfig();
			//创建tracker client
			TrackerClient trackerClient = new TrackerClient();
			//获取trackerServer
			TrackerServer trackerServer = trackerClient.getConnection();
			//获取storage
			StorageServer storageServer = trackerClient.getStoreStorage(trackerServer);
			//创建storage client
			StorageClient1 storageClient1 = new StorageClient1(trackerServer, storageServer);
			//上传文件
			//文件字节
			byte[] bytes = multipartFile.getBytes();
			//文件原始名称
			String originalFilename = multipartFile.getOriginalFilename();
			//文件扩展名
			String extName = originalFilename.substring(originalFilename.lastIndexOf(".")+1);
			//文件id
			String fileId = storageClient1.upload_file1(bytes, extName, null);
			
			return fileId;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
