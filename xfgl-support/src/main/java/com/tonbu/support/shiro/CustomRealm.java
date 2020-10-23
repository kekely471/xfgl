
package com.tonbu.support.shiro;

import com.tonbu.framework.data.UserEntity;
import com.tonbu.framework.util.ContainerUtils;
import com.tonbu.support.helper.JwtTokenHelper;
import com.tonbu.support.service.AccountService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;


public class CustomRealm extends AuthorizingRealm {
    Logger logger = LogManager.getLogger(CustomRealm.class.getName());

    @Autowired
    private AccountService accountService;

    /**
     * 获取身份验证信息
     * Shiro中，最终是通过 Realm 来获取应用程序中的用户、角色及权限信息的。
     *
     * @param token 用户身份信息 token
     * @return 返回封装了用户信息的 AuthenticationInfo 实例
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        logger.info("***用户身份验证");
        //获取用户的输入的账号.
        String username = (String) token.getPrincipal();
        if (StringUtils.isEmpty(username)) {
            return null;
        }
        //通过username从数据库中查找 User对象，如果找到，没找到.
        //实际项目中，这里可以根据实际情况做缓存，如果不做，Shiro自己也是有时间间隔机制，2分钟内不会重复执行该方法
        UserEntity userEntity = accountService.findByloginname(username);
        if (userEntity == null) {
            return null;
        }
        if (userEntity.getUsername() == null) {
            return null;
        }
        logger.info("***登录user：" + userEntity.getLoginname());
        ByteSource credentialsSalt = new Md5Hash(userEntity.getSalt());
        SimpleAuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(
                userEntity, //用户名
                userEntity.getPassword(), //密码
                credentialsSalt,//盐
                getName()  //realm name
        );
        return authenticationInfo;
    }

    /**
     * 获取授权信息
     *
     * @param principals
     * @return
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        System.out.println("权限配置-->CustomRealm.doGetAuthorizationInfo()");
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        UserEntity userEntity = (UserEntity) principals.getPrimaryPrincipal();
        for (Map role : userEntity.getRoles()) {
            authorizationInfo.addRole(role.get("rolename").toString());
        }
        for (Map p : userEntity.getMenus()) {
            authorizationInfo.addStringPermission(p.get("url").toString());
        }
        return authorizationInfo;
    }

    @PostConstruct
    public void setCredentialMatcher() {
        HashedCredentialsMatcher credentialsMatcher = new HashedCredentialsMatcher();
        credentialsMatcher.setHashAlgorithmName("MD5");
        credentialsMatcher.setHashIterations(1024);
        setCredentialsMatcher(credentialsMatcher);
    }


    public static UserEntity GetLoginUser() {
        Subject subject = SecurityUtils.getSubject();
        UserEntity u = (UserEntity) subject.getPrincipal();
        return u;
    }

    //加入登录用户信息缓存
    public static UserEntity GetLoginUser(String userToken) {
        JwtTokenHelper jwtTokenHelper =(JwtTokenHelper)  ContainerUtils.getBean("jwtTokenHelper");
        String  userId = jwtTokenHelper.getUsernameFromToken(userToken);
        AccountService accountService = (AccountService) ContainerUtils.getBean("accountService");
        UserEntity userEntity = accountService.findById(userId);
        return userEntity;
    }

    //加入登录用户信息缓存 无userId
    public static UserEntity GetMobileUser() {
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes sra = (ServletRequestAttributes)ra;
        HttpServletRequest request = sra.getRequest();
        JwtTokenHelper jwtTokenHelper =(JwtTokenHelper)  ContainerUtils.getBean("jwtTokenHelper");
        String  userId = jwtTokenHelper.getUsernameFromToken(request.getParameter("userId"));
        AccountService accountService = (AccountService) ContainerUtils.getBean("accountService");
        UserEntity userEntity = accountService.findById(userId);
        return userEntity;

    }

    //加入登录用户信息缓存
    public static UserEntity GetLoginUserByuserId(String userid) {
        AccountService accountService = (AccountService) ContainerUtils.getBean("accountService");
        UserEntity userEntity = accountService.findById(userid);
        return userEntity;
    }

}
