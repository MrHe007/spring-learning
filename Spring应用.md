思考问题：

1、如果做错误处理

# 解析spring 源码

[参考博客](https://blog.csdn.net/baomw/article/details/83956300) 

[官方文档](https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/core.html#beans-definition)

# 基础知识

## 注解开发

### 入门

#### 导入pom

```xml
 <dependencies>
     <dependency>
         <groupId>org.springframework</groupId>
         <artifactId>spring-context</artifactId>
         <version>4.3.12.RELEASE</version>
     </dependency>
     <dependency>
         <groupId>junit</groupId>
         <artifactId>junit</artifactId>
         <version>4.10</version>
         <scope>test</scope>
     </dependency>
</dependencies>
```

#### spring配置

```java
@Configuration
// 扫描包下的 compent
@ComponentScan(basePackages = "com.bigguy.spring.service")
public class SpringContext {
    
}
```

#### UserSvc

```java
@Service
public class UserSvc {
    public void sayHello(){
        System.out.println("hello , world!");
    }
}
```

#### 测试

```java
public static void main(String[] args) {
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(SpringContext.class);
    UserSvc userSvc = context.getBean(UserSvc.class);
    userSvc.sayHello();
}
```

## xml开发



# 源码分析

### 2.5容器的基础XmlBeanFactory



```java
BeanFactory  bf =  new  XmlBeanFactory (new ClassPathResource(” spring-context.xml ”));
```

#### 2.5.1文件封装

```java
public XmlBeanFactory(Resource resource) throws BeansException {
    this(resource, null);
}
```

内部其实是一个 Resource类型。spring 将资源文件抽象成了：URL，通过注册不同的协议如："file:"，"http:"， "jar:" 指定资源文件的加载路径



### 2.6、获取验证模式

#### DTD 和 XSD

DTD 全称是 Document Type Definition ，XSD 是 Xml Schemas Definition。

两种规则都是为了用来验证XML 文件的正确性。比如：<bean> 标签必须在<beans> 标签里面，还有标签的前后顺序.....

解析Spring-context.xml 中的 bean 定义前，首先会验证用户编写的 spring-context.xml 是否格式异常

##### DTD

下面是DTD 的格式：

1、DTD 会出现 'DOCTYPE' 的字符串，而XSD 则不会

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE beans PUBLIC "-//SPRING//DTD BEAN 2.0//EN" "http://www.springframework.org/dtd/spring-beans-2.0.dtd">
<beans>
	.....
</beans>
```



##### XSD

下面是XSD 的格式：

- 没有DOCTYPE 字符串
- xmlns 根据标签，指定了验证标签对应的 XSD 文件地址。刚开始会从网络中下载，后保存到仓库相应的路径下

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="
   http://www.springframework.org/schema/beans
   http://www.springframework.org/schema/beans/spring-beans-3.0.xsd">
</beans>
```

#### 验证模式读取

1. 先读取验证方式
2. 根据 XSD\DTD 验证
3. 读取spring-context.xml 里面定义的bean

```java
protected int doLoadBeanDefinitions(InputSource inputSource, Resource resource)
			throws BeanDefinitionStoreException {
		try {
			Document doc = doLoadDocument(inputSource, resource);
			return registerBeanDefinitions(doc, resource);
		}
		// ...
	}
```

```java
// 实际加载 document
protected Document doLoadDocument(InputSource inputSource, Resource resource) throws Exception {
    return this.documentLoader.loadDocument(inputSource, getEntityResolver(), this.errorHandler,
                                            getValidationModeForResource(resource), isNamespaceAware());
}

```

```java
protected int getValidationModeForResource(Resource resource) {
    int validationModeToUse = getValidationMode();
    // 如果手动指定了验证模式，使用指定的
    if (validationModeToUse != VALIDATION_AUTO) {
        return validationModeToUse;
    }
    // 加载资源查看匹配模式
    int detectedMode = detectValidationMode(resource);
    if (detectedMode != VALIDATION_AUTO) {
        return detectedMode;
    }
    return VALIDATION_XSD;
}
```

```java
protected int detectValidationMode(Resource resource) {
		if (resource.isOpen()) {
			//...
		}
		InputStream inputStream;
		try {
			inputStream = resource.getInputStream();
		}
		// ....
		try {
			return this.validationModeDetector.detectValidationMode(inputStream);
		}
	}
```

真正校验验证模式的地方

```java
// 循环读取 spring 配置文件的每一行，查看是否有 DOCTYPE 字符串，有的话就是 DTD 验证模式
public int detectValidationMode(InputStream inputStream) throws IOException {
		// Peek into the file to look for DOCTYPE.
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		try {
			boolean isDtdValidated = false;
			String content;
			while ((content = reader.readLine()) != null) {
				content = consumeCommentTokens(content);
				if (this.inComment || !StringUtils.hasText(content)) {
					continue;
				}
				if (hasDoctype(content)) {
					isDtdValidated = true;
					break;
				}
				if (hasOpeningTag(content)) {
					// End of meaningful data...
					break;
				}
			}
			return (isDtdValidated ? VALIDATION_DTD : VALIDATION_XSD);
		}
	}
```

### 2.8 解析注册BeanDefinitions



#### 2.8.1 profile 属性的使用

> profile 可以指定使用具体的环境

```xml
<beans profile="dev">
    
</beans>
<beans profile="product">
    
</beans>
```

#### 2.8.2 默认标签、自定义标签

默认标签：<bean> \ <import> \ <alias> \ <beans>

自定义标签：<tx:annotation-driven /> 

```java
protected void parseBeanDefinitions(Element root, BeanDefinitionParserDelegate delegate) {
    if (delegate.isDefaultNamespace(root)) {
        NodeList nl = root.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (node instanceof Element) {
                Element ele = (Element) node;
                if (delegate.isDefaultNamespace(ele)) {
                    // 解析默认标签
                    parseDefaultElement(ele, delegate);
                }
                else {
                     // 解析自定义标签
                    delegate.parseCustomElement(ele);
                }
            }
        }
    }
    else {
        // 解析自定义标签
        delegate.parseCustomElement(root);
    }
}
```

## 3、解析默认标签

DefaultBeanDefinitionDocumentReader

```java
private void parseDefaultElement(Element ele, BeanDefinitionParserDelegate delegate) {
    if (delegate.nodeNameEquals(ele, IMPORT_ELEMENT)) {
        importBeanDefinitionResource(ele);
    }
    else if (delegate.nodeNameEquals(ele, ALIAS_ELEMENT)) {
        processAliasRegistration(ele);
    }
    else if (delegate.nodeNameEquals(ele, BEAN_ELEMENT)) {
        processBeanDefinition(ele, delegate);
    }
    else if (delegate.nodeNameEquals(ele, NESTED_BEANS_ELEMENT)) {
        // recurse
        doRegisterBeanDefinitions(ele);
    }
}
```









































o