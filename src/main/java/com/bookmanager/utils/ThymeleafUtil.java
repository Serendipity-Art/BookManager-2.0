package com.bookmanager.utils;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.Thymeleaf;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.engine.ITemplateHandler;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.IOException;
import java.io.Writer;

/**
 * 正在努力学习Java的小白
 *
 * @author Serendipity
 * @date 2026/10/22
 */
public class ThymeleafUtil {
    //三大修饰符,private私有化,不可被外部使用,外部调用只能使用process,static类变量,属于类,全局一份,final一旦被使用就不可改变
    private static final TemplateEngine engine;
    //静态初始化块,类加载时候调用一次
    static {
        engine = new TemplateEngine();//创建模板引擎
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();//创建模板解释器,通过getResourceAsStream读取资源
        resolver.setPrefix("templates/");        // 设置前缀,资源统一放在resouces/templates目录下面
        resolver.setSuffix(".html");    //统一添加后缀
        resolver.setCharacterEncoding("UTF-8");
        resolver.setTemplateMode(TemplateMode.HTML);//设置模板模式
        engine.setTemplateResolver(resolver);//绑定解释器到引擎
    }
    private ThymeleafUtil() {
    }//私有化构造器,防止外部new()对象使用,工具类禁止实例化
    public static void process(String template, WebContext context, Writer writer){
        engine.process(template, context, writer);
    }
}
