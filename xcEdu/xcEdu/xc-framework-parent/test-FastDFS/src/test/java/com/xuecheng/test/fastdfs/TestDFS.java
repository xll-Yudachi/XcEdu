package com.xuecheng.test.fastdfs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.FileInfo;
import org.csource.fastdfs.StorageClient1;
import org.csource.fastdfs.StorageServer;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestDFS {

	//上传文件
	@Test
	public void testUpload() {
		try {
			//加载fastdfs-client.properties配置文件
			ClientGlobal.initByProperties("config/fastdfs-client.properties");
			//定义TrackerClient,用于请求TrackerServer
			TrackerClient trackerClient = new TrackerClient();
			//连接Tracker
			TrackerServer trackerServer = trackerClient.getConnection();
			//获取storage
			StorageServer storageServer = trackerClient.getStoreStorage(trackerServer);
			//创建storageclient
			StorageClient1 storageClient1 = new StorageClient1(trackerServer, storageServer);
			//向storage服务器上传文件
			//文件路径
			String filePath = "C:\\Users\\a\\Desktop\\商品图\\3.jpg";
			String fileId = storageClient1.upload_file1(filePath, "jpg", null);
			System.out.println(fileId);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//上传文件
	@Test
	public void testDownload() {
		try {
			//加载fastdfs-client.properties配置文件
			ClientGlobal.initByProperties("config/fastdfs-client.properties");
			//定义TrackerClient,用于请求TrackerServer
			TrackerClient trackerClient = new TrackerClient();
			//连接Tracker
			TrackerServer trackerServer = trackerClient.getConnection();
			//获取storage
			StorageServer storageServer = trackerClient.getStoreStorage(trackerServer);
			//创建storageclient
			StorageClient1 storageClient1 = new StorageClient1(trackerServer, storageServer);
			//从storage服务器下载文件
			//文件id
			//String fileId = "group1/M00/00/00/wKhQhF0LPjeAHissABKLQFhadxQ641.jpg";
			String fileId = "group1/M00/00/00/wKhQhF0LdhiAGqhbABAZukkVxCQ379.jpg";
			byte[] bytes = storageClient1.download_file1(fileId);
			//使用输出流保存文件
			FileOutputStream fos = new FileOutputStream(new File("C:\\Users\\a\\Desktop\\商品图\\4.jpg"));
			fos.write(bytes);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//查询文件
	@Test
	public void testQeury() {
		try {
			//加载fastdfs-client.properties配置文件
			ClientGlobal.initByProperties("config/fastdfs-client.properties");
			//定义TrackerClient,用于请求TrackerServer
			TrackerClient trackerClient = new TrackerClient();
			//连接Tracker
			TrackerServer trackerServer = trackerClient.getConnection();
			//获取storage
			StorageServer storageServer = trackerClient.getStoreStorage(trackerServer);
			//创建storageclient
			StorageClient1 storageClient1 = new StorageClient1(trackerServer, storageServer);
			//从storage服务器下载文件
			//文件id
			//String fileId = "group1/M00/00/00/wKhQhF0LPjeAHissABKLQFhadxQ641.jpg";
			String fileId = "group1/M00/00/00/wKhQhF0LdhiAGqhbABAZukkVxCQ379.jpg";
			FileInfo fileInfo = storageClient1.query_file_info1(fileId);
			System.out.println(fileInfo);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}





