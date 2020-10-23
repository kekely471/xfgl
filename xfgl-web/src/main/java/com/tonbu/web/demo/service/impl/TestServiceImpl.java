package com.tonbu.web.demo.service.impl;

import com.tonbu.web.demo.service.TestService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TestServiceImpl implements TestService {

    Logger logger=LogManager.getLogger(TestServiceImpl.class.getName());
    @Autowired
    @Qualifier("secondaryJdbcTemplate")
    protected JdbcTemplate jdbcTemplate;

    @Override
    public void create(String name, Integer age) {
        jdbcTemplate.update("insert into test(name,age,create_time,status) values(?,?,sysdate(),?)", name, age,1);
    }

    @Override
    public void deleteByName(String name) {
        jdbcTemplate.update("delete from test where name = ?", name);
    }

    @Override
    public Integer getAllUsers() {
        return jdbcTemplate.queryForObject("select count(1) from test", Integer.class);
    }

    @Override
    public void deleteAllUsers() {
        jdbcTemplate.update("delete from test");
    }


    @Override
    @Transactional
    public void TestTr()  {


        logger.trace("我是trace");
        logger.info("我是info信息");
        logger.error("我是error");
        logger.fatal("我是fatal");

    }
}
