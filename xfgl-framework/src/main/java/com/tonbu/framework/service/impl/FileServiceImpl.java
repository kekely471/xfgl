package com.tonbu.framework.service.impl;

import com.tonbu.framework.dao.DBHelper;
import com.tonbu.framework.service.FileService;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
public class FileServiceImpl implements FileService {


    /**
     * 获取附件数据源
     *
     * @return
     */
    public List<Map<String, Object>> loadFileList(Map<String, String> param) {
        String tableName = param.get("tableName");
        String keyName = param.get("keyName");
        String id = param.get("id");
        return DBHelper.queryForList("SELECT F.ID , F.FILE_NAME||'.'||F.EXT FILE_NAME,F.CREATE_TIME,F.FILE_SIZE FROM " + tableName + " T LEFT JOIN BUSINESS_FILE F ON F.ID = T.BUSINESS_FILE_ID WHERE T." + keyName + " = ?", id);
    }


    public void Upload(Map<String, String> filemap) {
        DBHelper.execute("INSERT INTO BUSINESS_FILE(ID,FILE_NAME,FILE_SIZE,PATH,EXT,CONTENT_TYPE,CREATE_TIME) VALUES (?,?,?,?,?,?,SYSDATE)",
                filemap.get("id"), filemap.get("name"), filemap.get("size"), filemap.get("path"), filemap.get("ext"), filemap.get("ctype"));
    }

    public void UploadBatch(List<Map<String, String>> filelist) {
        for (Map<String, String> filemap :
                filelist) {
            DBHelper.execute("INSERT INTO BUSINESS_FILE(ID,FILE_NAME,FILE_SIZE,PATH,EXT,CONTENT_TYPE,CREATE_TIME) VALUES (?,?,?,?,?,?,SYSDATE)",
                    filemap.get("id"), filemap.get("name"), filemap.get("size"), filemap.get("path"), filemap.get("ext"), filemap.get("ctype"));
        }

    }


    public Map<String, Object> download(String id) {
        return DBHelper.queryForMap("SELECT F.ID , F.FILE_NAME||'.'||F.EXT FILE_NAME,F.CREATE_TIME,F.FILE_SIZE,F.PATH,F.EXT FROM BUSINESS_FILE F WHERE F.ID = ?", id);
    }

    public Map<String, Object> getFileInfo(Map<String, String> param) {
        return DBHelper.queryForMap("SELECT F.ID ,F.PATH, F.FILE_NAME||'.'||F.EXT FILE_NAME,F.CONTENT_TYPE,F.CREATE_TIME,F.FILE_SIZE from BUSINESS_FILE f where id = ?", param.get("id"));
    }

    public Map<String, Object> downloadBytes(String id) {
        return DBHelper.queryForMap("SELECT F.ID , F.FILE_NAME||'.'||F.EXT FILE_NAME,F.CREATE_TIME,F.FILE_SIZE,F.PATH,F.EXT,CONTENT FROM BUSINESS_FILE F WHERE F.ID = ?", new Object[]{id});
    }

    public void UploadBatchBytes(List<Map<String, Object>> filelist){
        for (Map<String, Object> filemap :filelist) {
            DBHelper.execute("INSERT INTO BUSINESS_FILE(ID,FILE_NAME,FILE_SIZE,EXT,CONTENT_TYPE,CREATE_TIME,CONTENT) VALUES (?,?,?,?,?,SYSDATE,?)", new Object[]{filemap.get("id"), filemap.get("name"), filemap.get("size"), filemap.get("ext"), filemap.get("ctype"),new Object[]{"blob",filemap.get("content")}});
        }
    }
}
