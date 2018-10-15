package com.lcx.util;


import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class HttpUtil {

	private static final Logger logger = LoggerFactory
			.getLogger(HttpUtil.class);
	// 连接超时时间
	private static final int CONNECTION_TIMEOUT = 60000;// 60秒
	// 读数据超时时间
	private static final int READ_DATA_TIMEOUT = 60000;// 60秒

	private static PoolingHttpClientConnectionManager connManager = null;
	private static CloseableHttpClient httpclient = null;
	static {
		connManager = new PoolingHttpClientConnectionManager();
		httpclient = HttpClients.custom().setConnectionManager(connManager)
				.build();
	}

	/**
	 * sslClient
	 * 
	 * @return
	 */
	private static CloseableHttpClient createSSLClient() {
		try {
			SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(
					null, new TrustStrategy() {
						// 信任所有
						@Override
						public boolean isTrusted(X509Certificate[] chain,
								String authType) throws CertificateException {
							return true;
						}
					}).build();
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
					sslContext,
					SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			return HttpClients.custom().setSSLSocketFactory(sslsf).build();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return HttpClients.createDefault();
	}
	
	/**
	 * post请求，如果失败尝试3次
	 * @param url
	 * @param data
	 * @param encoding
	 * @return
	 */
	public static String tryPost(String url, Map<String, String> data, String encoding) {
		String resultStr = "";
		for (int i = 0; i < 3; i++) {
			try {
				resultStr = post(StringUtils.trim(url), data, encoding);
				break;
			} catch (Exception e) {
				logger.error("请求异常count:{连接地址："+url+"，次数} " + i, e);
			}
		}
		return resultStr;
	}

	/**
	 * Post请求（默认超时时间）
	 * 
	 * @param url
	 * @param data
	 * @param encoding
	 * @return
	 */
	public static String post(String url, Map<String, String> data,
			String encoding) throws IOException {
		return post(url, CONNECTION_TIMEOUT, READ_DATA_TIMEOUT, data, encoding);
	}
	
	public static String post(String url, String data,
			String encoding) throws IOException {
		return post(url, CONNECTION_TIMEOUT, READ_DATA_TIMEOUT, data, encoding);
	}

	public static String post(String url, int timeout,
			Map<String, String> data, String encoding) throws IOException {
		return post(url, timeout, timeout, data, encoding);
	}

	/**
	 * Post请求
	 * 
	 * @param url
	 * @param connectTimeout
	 * @param readTimeout
	 * @param data
	 * @param encoding
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */
	private static String post(String url, int connectTimeout, int readTimeout,
			Map<String, String> data, String encoding) throws IOException {
		HttpPost post = new HttpPost(url);
		RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(readTimeout)
				.setConnectTimeout(connectTimeout)
				.setConnectionRequestTimeout(connectTimeout)
				.setExpectContinueEnabled(false).build();
		post.setConfig(requestConfig);

		if (null != data && !data.isEmpty()) {
			List<NameValuePair> formparams = new ArrayList<NameValuePair>();
			for (String key : data.keySet()) {
				String val = data.get(key);
				if(StringUtils.isNotBlank(val)){
					formparams.add(new BasicNameValuePair(key, val));
				}
			}
			UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(
					formparams, encoding);
			post.setEntity(formEntity);
		}
		CloseableHttpResponse response = null;
		if (url.startsWith("https")) {// https
			response = createSSLClient().execute(post);
		} else {
			response = httpclient.execute(post);
		}

		HttpEntity entity = response.getEntity();
		try {
			if (entity != null) {
				String str = EntityUtils.toString(entity, encoding);
				return str;
			}
		} finally {
			if (entity != null) {
				entity.getContent().close();
			}
			if (response != null) {
				response.close();
			}
		}
		return null;
	}
	
	/**
	 * Post请求
	 * 
	 * @param url
	 * @param connectTimeout
	 * @param readTimeout
	 * @param data
	 * @param encoding
	 * @return
	 * @throws IOException
	 */
	private static String post(String url, int connectTimeout, int readTimeout,
			String data, String encoding) throws IOException{
		HttpPost post = new HttpPost(url);
		RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(readTimeout)
				.setConnectTimeout(connectTimeout)
				.setConnectionRequestTimeout(connectTimeout)
				.setExpectContinueEnabled(false).build();
		post.setConfig(requestConfig);
		post.setEntity(new StringEntity(data,encoding));
		CloseableHttpResponse response = null;
		if (url.startsWith("https")) {// https
			response = createSSLClient().execute(post);
		} else {
			response = httpclient.execute(post);
		}

		HttpEntity entity = response.getEntity();
		try {
			if (entity != null) {
				String str = EntityUtils.toString(entity, encoding);
				return str;
			}
		} finally {
			if (entity != null) {
				entity.getContent().close();
			}
			if (response != null) {
				response.close();
			}
		}
		return null;
	}

	/**
	 * get请求，如果失败尝试3次
	 * 
	 * @param url
	 * @param encoding
	 * @return
	 */
	public static String tryGet(String url, String encoding) {
		String resultStr = "";
		for (int i = 0; i < 3; i++) {
			try {
				resultStr = get(url, encoding);
				break;
			} catch (Exception e) {
				logger.error("请求异常count:{连接地址："+url+"} ", i, e);
			}
		}
		return resultStr;
	}
	
	/**
	 * get请求（默认超时时间）
	 * 
	 * @param url
	 * @param encoding
	 * @return
	 */
	public static String get(String url, String encoding) throws IOException {
		return get(url, null, CONNECTION_TIMEOUT, READ_DATA_TIMEOUT, encoding);
	}

	public static String get(String url, Map<String, String> cookies,
			String encoding) throws IOException {
		return get(url, cookies, CONNECTION_TIMEOUT, READ_DATA_TIMEOUT,
				encoding);
	}

	public static String get(String url, Map<String, String> cookies,
			int timeout, String encoding) throws IOException {
		return get(url, cookies, timeout, timeout, encoding);
	}

	private static String get(String url, Map<String, String> cookies,
			int connectTimeout, int readTimeout, String encoding)
			throws IOException {
		HttpGet get = new HttpGet(url);
		RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(readTimeout)
				.setConnectTimeout(connectTimeout)
				.setConnectionRequestTimeout(connectTimeout)
				.setExpectContinueEnabled(false).build();
		get.setConfig(requestConfig);
		if (cookies != null && !cookies.isEmpty()) {
			StringBuilder buffer = new StringBuilder(128);
			for (String cookieKey : cookies.keySet()) {
				buffer.append(cookieKey).append("=")
						.append(cookies.get(cookieKey)).append("; ");
			}
			// 设置cookie内容
			get.setHeader(new BasicHeader("Cookie", buffer.toString()));
		}
		CloseableHttpResponse response = null;
		if (url.startsWith("https")) {// https
			response = createSSLClient().execute(get);
		} else {
			response = httpclient.execute(get);
		}
		HttpEntity entity = response.getEntity();
		try {
			if (entity != null) {
				String str = EntityUtils.toString(entity, encoding);
				return str;
			}
		} finally {
			if (entity != null) {
				entity.getContent().close();
			}
			if (response != null) {
				response.close();
			}
		}

		return null;
	}

	/**
	 * map转成queryStr
	 * 
	 * @param paramMap
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String mapToQueryStr(Map<String, String> paramMap) {
		StringBuffer strBuff = new StringBuffer();
		for (String key : paramMap.keySet()) {
			strBuff.append(key).append("=").append(paramMap.get(key))
					.append("&");
		}
		return strBuff.substring(0, strBuff.length() - 1);
	}

	
	/**
	 * 使用POST方式上传文件
	 * @param url 
	 * @param name 文件参数名
	 * @param filename 文件名
	 * @param input 文件流
	 * @param params 额外参数
	 * @param connectTimeout
	 * @param readTimeout
	 * @param encoding 返回响应字符串编码
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static String postFile(String url, String name, String filename, InputStream input, Map<String,String> params, 
			int connectTimeout, int readTimeout, String encoding) throws ClientProtocolException, IOException{
		HttpPost post = new HttpPost(url);
		RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(readTimeout)
				.setConnectTimeout(connectTimeout)
				.setConnectionRequestTimeout(connectTimeout)
				.setExpectContinueEnabled(false).build();
		post.setConfig(requestConfig);
		
		//组装文件和其他参数
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		//二进制文件
		builder.addBinaryBody(name, input, ContentType.DEFAULT_BINARY, filename);
		//额外参数
		if(MapUtils.isNotEmpty(params)){
			for(Map.Entry<String,String> param : params.entrySet()){
				builder.addTextBody(param.getKey(), param.getValue(), ContentType.TEXT_PLAIN);
			}
		}
		HttpEntity httpEntity = builder.build();
		post.setEntity(httpEntity);
		
		CloseableHttpResponse response = null;
		HttpEntity resultEntity = null;
		try{
			if (url.startsWith("https")) {// https
				response = createSSLClient().execute(post);
			} else {
				response = httpclient.execute(post);
			}

			resultEntity = response.getEntity();
			if (resultEntity != null) {
				return EntityUtils.toString(resultEntity, encoding);
			}
		}finally{
			if (resultEntity != null) {
				resultEntity.getContent().close();
			}
			if (response != null) {
				response.close();
			}
		}
		return null;
	}
	
	/**
	 * 使用POST方式上传文件
	 * @param url
	 * @param name 文件参数名
	 * @param filename 文件名
	 * @param input 文件流
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static String postFile(String url, String name, String filename, InputStream input) throws ClientProtocolException, IOException{
		return postFile(url, name, filename, input, null, CONNECTION_TIMEOUT, READ_DATA_TIMEOUT, CharEncoding.UTF_8);
	}
	
	/**
	 * 使用POST方式上传文件
	 * @param url
	 * @param name 文件参数名
	 * @param filename 文件名
	 * @param input 文件流
	 * @param encoding 返回响应字符串编码
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static String postFile(String url, String name, String filename, InputStream input, String encoding) throws ClientProtocolException, IOException{
		return postFile(url, name, filename, input, null, CONNECTION_TIMEOUT, READ_DATA_TIMEOUT, encoding);
	}
	
	/**
	 * 使用POST方式上传文件
	 * @param url
	 * @param name 文件参数名
	 * @param filename 文件名
	 * @param input 文件流
	 * @param params 额外参数
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static String postFile(String url, String name, String filename, InputStream input, Map<String,String> params) throws ClientProtocolException, IOException{
		return postFile(url, name, filename, input, params, CONNECTION_TIMEOUT, READ_DATA_TIMEOUT, CharEncoding.UTF_8);
	}
	
	/**
	 * 使用POST方式上传文件
	 * @param url 
	 * @param name 文件参数名
	 * @param filename 文件名
	 * @param input 文件流
	 * @param params 额外参数
	 * @param encoding 返回响应字符串编码
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static String postFile(String url, String name, String filename, InputStream input, Map<String,String> params, String encoding) throws ClientProtocolException, IOException{
		return postFile(url, name, filename, input, params, CONNECTION_TIMEOUT, READ_DATA_TIMEOUT, encoding);
	}

	
	public static String postFiles(String url, Map<Integer, MultipartFile> files, Map<String, String> params) throws ClientProtocolException, IOException{
		return postFiles(url, files, params, CONNECTION_TIMEOUT, READ_DATA_TIMEOUT, CharEncoding.UTF_8);
	}
	

	/**
	 * 使用POST方式上传文件
	 * @param url 
	 * @param files 多个文件
	 * @param params 额外参数
	 * @param connectTimeout
	 * @param readTimeout
	 * @param encoding 返回响应字符串编码
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static String postFiles(String url, Map<Integer, MultipartFile> files, Map<String,String> params, 
			int connectTimeout, int readTimeout, String encoding) throws ClientProtocolException, IOException{
		HttpPost post = new HttpPost(url);
		RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(readTimeout)
				.setConnectTimeout(connectTimeout)
				.setConnectionRequestTimeout(connectTimeout)
				.setExpectContinueEnabled(false).build();
		post.setConfig(requestConfig);
		
		//组装文件和其他参数
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		if (MapUtils.isNotEmpty(files)) {
			//遍历文件
			for (Integer key : files.keySet()) {
				//二进制文件
				builder.addBinaryBody(files.get(key).getName(), files.get(key).getInputStream(), ContentType.DEFAULT_BINARY,
						files.get(key).getOriginalFilename());

			} 
		}
		//额外参数
		if(MapUtils.isNotEmpty(params)){
			for(Map.Entry<String,String> param : params.entrySet()){
				builder.addTextBody(param.getKey(), param.getValue(), ContentType.APPLICATION_JSON);
			}
		}
		HttpEntity httpEntity = builder.build();
		post.setEntity(httpEntity);
		CloseableHttpResponse response = null;
		HttpEntity resultEntity = null;
		try{
			if (url.startsWith("https")) {// https
				response = createSSLClient().execute(post);
			} else {
				response = httpclient.execute(post);
			}

			resultEntity = response.getEntity();
			if (resultEntity != null) {
				return EntityUtils.toString(resultEntity, encoding);
			}
		}finally{
			if (resultEntity != null) {
				resultEntity.getContent().close();
			}
			if (response != null) {
				response.close();
			}
		}
		return null;
	}

}
