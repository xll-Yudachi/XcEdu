package com.xuecheng.manage_media_process.mq;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.MediaFileProcess_m3u8;
import com.xuecheng.framework.utils.HlsVideoUtil;
import com.xuecheng.framework.utils.Mp4VideoUtil;
import com.xuecheng.manage_media_process.dao.MediaFileRepository;

@Component
public class MediaProcessTask {

	@Autowired
	private MediaFileRepository mediaFileRepository;
	
	//ffmpeg路径
	@Value("${xc-service-manage-media.ffmpeg-path}")
    public String ffmpeg_path;
	//上传文件根目录
	@Value("${xc-service-manage-media.video-location}")
	public String serverPath;
	
	//接收视频处理消息进行视频处理
	@RabbitListener(queues = "${xc-service-manage-media.mq.queue-media-video-processor}",containerFactory="customContainerFactory")
	public void receiveMediaProcessTask(String msg) {
		//ts文件列表
		List<String> ts_list = null;
		
		//1.解析消息内容，得到mediaId
		Map msgMap = JSON.parseObject(msg,Map.class);
		String mediaId = (String)msgMap.get("mediaId");
		//2.拿到MediaID从数据库查询文件信息
		Optional<MediaFile> optional = mediaFileRepository.findById(mediaId);
		if(!optional.isPresent()) {
			return ;
		}
		MediaFile mediaFile = optional.get();
		//3.使用工具类将avi文件生成Mp4 将Mp4转换成m3u8和ts文件/或者直接将mp4文件生成m3u8文件和ts文件
		String fileType = mediaFile.getFileType();
		//其他类型视频
		if(fileType == null || !fileType.equals("avi")) {
			if(fileType.equals("mp4")) {
				ts_list = mp4Process(mediaFile);
			}
		}else {
			aviProcess(mediaFile);
			ts_list = mp4Process(mediaFile);
		}
		
		//更新处理状态为成功
		mediaFile.setProcessStatus("303002");
		MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
		mediaFileProcess_m3u8.setTslist(ts_list);
		mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
		//m3u8文件Url
		mediaFile.setFileUrl(mediaFile.getFilePath() + "hls/" + mediaFile.getFileId() + ".m3u8");
		mediaFileRepository.save(mediaFile);
	}
	
	//avi格式视频处理(avi转mp4)
	private void aviProcess(MediaFile mediaFile) {
		mediaFile.setProcessStatus("303001");
		mediaFileRepository.save(mediaFile);
		//生成mp4
		String video_path = serverPath + mediaFile.getFilePath() + mediaFile.getFileName();
		String mp4_name = mediaFile.getFileId() + ".mp4";
		String mp4folder_path = serverPath + mediaFile.getFilePath();
		Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpeg_path, video_path, mp4_name, mp4folder_path);
		String result = videoUtil.generateMp4();
		if(result == null || !result.equals("success")) {
			//操作失败
			mediaFile.setProcessStatus("303003");
			MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
			mediaFileProcess_m3u8.setErrormsg(result);
			mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
			mediaFileRepository.save(mediaFile);
			return ;
		}
	}
	
	//生成m3u8(mp4转换成m3u8+ts文件)
	private List<String> mp4Process(MediaFile mediaFile) {
		String mp4_path = serverPath + mediaFile.getFilePath() + mediaFile.getFileId() + ".mp4";
		String m3u8_name = mediaFile.getFileId() + ".m3u8";
		String m3u8folder_path = serverPath + mediaFile.getFilePath() + "hls/";
		HlsVideoUtil hlsVideoUtil = new HlsVideoUtil(ffmpeg_path, mp4_path, m3u8_name, m3u8folder_path);
		String result = hlsVideoUtil.generateM3u8();
		if(result == null || !result.equals("success")) {
			//操作失败
			mediaFile.setProcessStatus("303003");
			MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
			mediaFileProcess_m3u8.setErrormsg(result);
			mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
			mediaFileRepository.save(mediaFile);
			return null;
		}
		List<String> ts_list = hlsVideoUtil.get_ts_list();
		return ts_list;
	}
}
