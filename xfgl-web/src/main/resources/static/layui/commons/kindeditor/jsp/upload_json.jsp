<%@page import="com.tonbusoft.pams.commons.utils.StringUtil"%>
<%@page import="com.tonbusoft.pams.bean.XtRyb"%>
<%@page import="net.sf.json.JSONArray"%>
<%@page import="net.sf.json.JSONObject"%>
<%@page import="net.sf.json.JSON"%>
<%@page import="com.tonbusoft.pams.commons.utils.ZipUtil"%>
<%@page import="com.tonbusoft.pams.commons.UserSession"%>
<%@page import="org.springframework.web.util.WebUtils"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="java.util.*,java.io.*"%>
<%@ page import="java.text.SimpleDateFormat"%>
<%@ page import="org.apache.commons.fileupload.*"%>
<%@ page import="org.apache.commons.fileupload.disk.*"%>
<%@ page import="org.apache.commons.fileupload.servlet.*"%>
<%@ page import="org.json.simple.*"%>
<%

//文件保存目录路径
String savePath = pageContext.getServletContext().getRealPath("/") + "attached/";
//文件保存目录URL
String saveUrl  = request.getContextPath() + "/attached/";
//定义允许上传的文件扩展名
HashMap<String, String> extMap = new HashMap<String, String>();
extMap.put("image", "gif,jpg,jpeg,png,bmp");
extMap.put("flash", "swf,flv");
extMap.put("media", "swf,flv,mp3,wav,wma,wmv,mid,avi,mpg,asf,rm,rmvb");
extMap.put("file", "xls,xlsx,ppt,doc,docx,pdf,txt,swf,zip,rar,xml");
extMap.put("all", "gif,jpg,jpeg,png,bmp,swf,flv,mp3,wav,wma,wmv,mid,avi,mpg,asf,rm,rmvb,xls,xlsx,ppt,doc,docx,pdf,txt,zip,rar,xml");

//最大文件大小
long maxSize = 209715200;// 200M大小

response.setContentType("text/html; charset=UTF-8");

if(!ServletFileUpload.isMultipartContent(request)){
	out.println(getError("请选择文件。"));
	return;
}
//检查目录
File uploadDir = new File(savePath);
if(!uploadDir.isDirectory()){
	/* out.println(getError("上传目录不存在。"));
	return; */
	uploadDir.mkdirs();
}
//检查目录写权限
if(!uploadDir.canWrite()){
	out.println(getError("上传目录没有写权限。"));
	return;
}

FileItemFactory factory = new DiskFileItemFactory();
ServletFileUpload upload = new ServletFileUpload(factory);
upload.setHeaderEncoding("UTF-8");
List items = upload.parseRequest(request);

String dirName = request.getParameter("dir");
if (dirName == null) {
	dirName = getDirName(extMap,((FileItem)items.get(0)).getName());//"image";
}
if(!extMap.containsKey(dirName)){
	out.println(getError("目录名不正确。"));
	return;
}
//创建文件夹
savePath += dirName + "/";
saveUrl += dirName + "/";
File saveDirFile = new File(savePath);
if (!saveDirFile.exists()) {
	saveDirFile.mkdirs();
}
SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
String ymd = sdf.format(new Date());
savePath += ymd + "/";
saveUrl += ymd + "/";
File dirFile = new File(savePath);
if (!dirFile.exists()) {
	dirFile.mkdirs();
}
String type=pageContext.getRequest().getParameter("type");
String opt =pageContext.getRequest().getParameter("opt");// 操作类型(add:新增；edit:编辑)
Iterator itr = items.iterator();
while (itr.hasNext()) {
	FileItem item = (FileItem) itr.next();
	String fileName = item.getName();
	long fileSize = item.getSize();
    String size = (fileSize >= 1024*1024) ? fileSize/(1024*1024) + "m" : fileSize/1024 + "k";
	if (!item.isFormField()) {
		//检查文件大小
		System.out.println("==="+item.getSize());
		if(item.getSize() > maxSize){
			out.println(getError("上传文件大小超过限制。"));
			return;
		}
		//检查扩展名
		String fileExt = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
		System.out.print(fileExt);
		//获取文件名
		String wjmName = "";
		if(fileName.indexOf("\\")!=-1){
			wjmName = fileName.substring(fileName.lastIndexOf("\\")+1,fileName.length());
		}else{
			wjmName = fileName;
		}
		//解压缩zip
		if(type!=null&&type.equals("zip")){
			if(!fileExt.equalsIgnoreCase("zip")){
				out.println(getError("上传文件扩展名是不允许的扩展名,只允许zip"));
				return;	
			}
		}
		//  
		if(type!=null&&type.equals("personXml")){
			if(!fileExt.equalsIgnoreCase("xml")){
				out.println(getError("上传文件扩展名是不允许的扩展名,只允许xml"));
				return;	
			}
		}

		if(!Arrays.<String>asList(extMap.get(dirName).split(",")).contains(fileExt)){
			out.println(getError("上传文件扩展名是不允许的扩展名,只允许" + extMap.get(dirName) + "格式。"));
			return;
		}
		
		//SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		//String newFileName = df.format(new Date()) + "_" + new Random().nextInt(1000) + "." + fileExt;
		String newFileName = StringUtil.randomUUID() + "." + fileExt;
		try{
			File uploadedFile = new File(savePath, newFileName);
			item.write(uploadedFile);
		}catch(Exception e){
			out.println(getError("上传文件失败。"));
			return;
		}
		
		JSONObject obj = new JSONObject();
		obj.put("error", 0);
		obj.put("url", saveUrl + newFileName);
		obj.put("filename", wjmName);
        obj.put("filesize", size);
		
		
		if(type!=null&&type.equals("zip")){
			 JSONObject json=ZipUtil.unzip(savePath+newFileName, savePath, opt);
			 if(json.containsKey("flag")&&!json.getBoolean("flag")){
				 out.println(getError(json.getString("msg")));
				 return;
			 }else
			 obj.put("lxxx", json);
		} 
		
		if(type!=null&&type.equals("personXml")){
			UserSession userSession = (UserSession) WebUtils.getSessionAttribute(request, "userSession");
			XtRyb jgUser = userSession.getXtRyb();
			 JSONArray jsonArray=ZipUtil.personXmlImp(savePath+newFileName, savePath, jgUser);
			 obj.put("userInfo", jsonArray);
		} 
		out.println(obj.toString());
	}
}
%>
<%!
private String getError(String message) {
	JSONObject obj = new JSONObject();
	obj.put("error", 1);
	obj.put("message", message);
	return obj.toString();
}
private String getDirName(HashMap<String, String> extMap, String fileName){
	
	
	String suffix = fileName.substring(fileName.lastIndexOf(".")+1).toLowerCase();
	
	for(String key : extMap.keySet()){
		if(Arrays.<String>asList(extMap.get(key).split(",")).contains(suffix)){
			return key;
		}
	}
	return "all";
}
%>