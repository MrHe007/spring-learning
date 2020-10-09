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

#### 2.8.3、BeanDefinitionHolder

> 解析 <bean> 标签，将bean 的一些数据装填到该对象中

````java
public class BeanDefinitionHolder implements BeanMetadataElement {

	private final BeanDefinition beanDefinition;

	private final String beanName;

	private final String[] aliases;
    
}
````

#### 2.8.4 BeanDefinitionParserDelegate

> 用于解析 spring-config.xml 的类。维护很多全局数据

##### 属性

```java
/**
	 * Stores all used bean names so we can enforce uniqueness on a per
	 * beans-element basis. Duplicate bean ids/names may not exist within the
	 * same level of beans element nesting, but may be duplicated across levels.
	 */
// 解析 spring-config.xml 中的 bean 标签时，放在该容器中。用于校验 beanName 和 beanAlias 的唯一性
private final Set<String> usedNames = new HashSet<String>();
```

```java
// 解析 <bean> 标签时，校验bean 的唯一性。beanName 不能和 beanAlias 同名
protected void checkNameUniqueness(String beanName, List<String> aliases, Element beanElement) {
    String foundName = null;

    if (StringUtils.hasText(beanName) && this.usedNames.contains(beanName)) {
        foundName = beanName;
    }
    if (foundName == null) {
        foundName = CollectionUtils.findFirstMatch(this.usedNames, aliases);
    }
    if (foundName != null) {
        error("Bean name '" + foundName + "' is already used in this <beans> element", beanElement);
    }

    this.usedNames.add(beanName);
    this.usedNames.addAll(aliases);
}
```

解析<bean> 标签

```java
public BeanDefinitionHolder parseBeanDefinitionElement(Element ele, BeanDefinition containingBean) {
    	// 解析id
		String id = ele.getAttribute(ID_ATTRIBUTE);
    	// 解析 name
		String nameAttr = ele.getAttribute(NAME_ATTRIBUTE);
		// 解析别名。别名是有多个，用",", 或者" "隔开
		List<String> aliases = new ArrayList<String>();
		if (StringUtils.hasLength(nameAttr)) {
			String[] nameArr = StringUtils.tokenizeToStringArray(nameAttr, MULTI_VALUE_ATTRIBUTE_DELIMITERS);
			aliases.addAll(Arrays.asList(nameArr));
		}

		if (containingBean == null) {
            // 校验 bean 的beanName 和 alias 的唯一性
			checkNameUniqueness(beanName, aliases, ele);
		}

		AbstractBeanDefinition beanDefinition = parseBeanDefinitionElement(ele, beanName, containingBean);
		if (beanDefinition != null) {
			if (!StringUtils.hasText(beanName)) {
				try {
					if (containingBean != null) {
						beanName = BeanDefinitionReaderUtils.generateBeanName(
								beanDefinition, this.readerContext.getRegistry(), true);
					}
					else {
						beanName = this.readerContext.generateBeanName(beanDefinition);
						
						String beanClassName = beanDefinition.getBeanClassName();
						if (beanClassName != null &&
								beanName.startsWith(beanClassName) && beanName.length() > beanClassName.length() &&
								!this.readerContext.getRegistry().isBeanNameInUse(beanClassName)) {
							aliases.add(beanClassName);
						}
					}
				}
				catch (Exception ex) {
				}
			}
			String[] aliasesArray = StringUtils.toStringArray(aliases);
			return new BeanDefinitionHolder(beanDefinition, beanName, aliasesArray);
		}
		return null;
	}
```

### 2.9、别名注册器

> 一个 beanName 可以有多个别名在 <bean> 标签中指定。或者 @Bean

```java
public interface AliasRegistry {

	void registerAlias(String name, String alias);
	
	void removeAlias(String alias);

	boolean isAlias(String name);

	String[] getAliases(String name);
}
```

SimpleAliasRegistry

> 别名放在 map 的key中，value 中放 beanName

```java
public class SimpleAliasRegistry implements AliasRegistry {

	private final Map<String, String> aliasMap = new ConcurrentHashMap<String, String>(16);

	@Override
	public void registerAlias(String name, String alias) {
		Assert.hasText(name, "'name' must not be empty");
		Assert.hasText(alias, "'alias' must not be empty");
		if (alias.equals(name)) {
			this.aliasMap.remove(alias);
		}
		else {
			String registeredName = this.aliasMap.get(alias);
			if (registeredName != null) {
				if (registeredName.equals(name)) {
					// An existing alias - no need to re-register
					return;
				}
				if (!allowAliasOverriding()) {
					throw new IllegalStateException("Cannot register alias '" + alias + "' for name '" +
							name + "': It is already registered for name '" + registeredName + "'.");
				}
			}
			checkForAliasCircle(name, alias);
			this.aliasMap.put(alias, name);
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

### 3.1、bean标签解析及注册

#### 3.1.1 解析BeanDefinition

1. BeanDefinition 是一个接口。在Spring中存在三种实现：RootBeanDefinition、ChildBeanDefinition、GenericBeanDefinition。
2. AbstractBeanDefinition 是 BeanDefinition <bean> 标签在容器中的表现
3. <bean> 拥有 beanClass、scope、lazyInit 属性
4. Spring 解析 BeanDefinition 并将所有的BeanDefinition 注册到 BeanDefinitionRegistry 中，类似数据库。后续所有针对 BeanDefinition 都是从 BeanDefinitionRegistry中取



解析<bean> 标签

```java
public AbstractBeanDefinition parseBeanDefinitionElement(
			Element ele, String beanName, BeanDefinition containingBean) {

		this.parseState.push(new BeanEntry(beanName));

		String className = null;
		if (ele.hasAttribute(CLASS_ATTRIBUTE)) {
			className = ele.getAttribute(CLASS_ATTRIBUTE).trim();
		}
		try {
			String parent = null;
			if (ele.hasAttribute(PARENT_ATTRIBUTE)) {
				parent = ele.getAttribute(PARENT_ATTRIBUTE);
			}
			AbstractBeanDefinition bd = createBeanDefinition(className, parent);
			// 解析属性：scope、singleton、abstract、lazy-nit、autowire、dependency-check、primary、init-method、factory-bean、factory-method
			parseBeanDefinitionAttributes(ele, beanName, containingBean, bd);
			bd.setDescription(DomUtils.getChildElementValueByTagName(ele, DESCRIPTION_ELEMENT));

			parseMetaElements(ele, bd);
			parseLookupOverrideSubElements(ele, bd.getMethodOverrides());
			parseReplacedMethodSubElements(ele, bd.getMethodOverrides());
			// 解析构造函数
			parseConstructorArgElements(ele, bd);
             // 解析propertity 标签
			parsePropertyElements(ele, bd);
			parseQualifierElements(ele, bd);

			bd.setResource(this.readerContext.getResource());
			bd.setSource(extractSource(ele));
			return bd;
		}
		finally {
			this.parseState.pop();
		}
		return null;
	}
```







#### 3.1.3 相关类





##### BeanDefinitionRegistry

> BeanDefinition 的注册中心

```java
public interface BeanDefinitionRegistry extends AliasRegistry {

	// 注册bean
    void registerBeanDefinition(String beanName, BeanDefinition beanDefinition);
	// 去掉bean
	void removeBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;
	// 通过beanName 获取bean详情
	BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;
	// 判断是否包含bean
	boolean containsBeanDefinition(String beanName);
	// 获取所有的beanName
	String[] getBeanDefinitionNames();
	// 获取bean个数
	int getBeanDefinitionCount();

	boolean isBeanNameInUse(String beanName);

}
```

##### SimpleBeanDefinitionRegistry

> 1、简单的bean定义注册中心
>
> 2、继承了bean别名注册中心

```java
public class SimpleBeanDefinitionRegistry extends SimpleAliasRegistry implements BeanDefinitionRegistry {

	/** Map of bean definition objects, keyed by bean name */
	private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<String, BeanDefinition>(64);
}
```

##### AliasRegistry

> 别名注册中心

```java
public interface AliasRegistry {

	void registerAlias(String name, String alias);

	void removeAlias(String alias);

	boolean isAlias(String name);

	String[] getAliases(String name);

}
```

##### SimpleAliasRegistry

> 简单的别名注册器：<key, value>  --  <beanAlias，beanName>

```java
public class SimpleAliasRegistry implements AliasRegistry {

	/** Map from alias to canonical name */
	private final Map<String, String> aliasMap = new ConcurrentHashMap<String, String>(16);

}
```

#### 3.1.4、注册解析 BeanDefinition

> 1、解析完 BeanDefinition 后，装配到 bean 注册中心中
>
> 2、XmlReaderContext 中的 AbstractBeanDefinitionReader 属性中存在 Bean解析中的一些数据：bean注册中心
>
> 3、bean注册中心实现了 bean别名注册中心

```java
public static void registerBeanDefinition(
    BeanDefinitionHolder definitionHolder, BeanDefinitionRegistry registry){
    // Register bean definition under primary name.
    String beanName = definitionHolder.getBeanName();
    registry.registerBeanDefinition(beanName, definitionHolder.getBeanDefinition());

    // Register aliases for bean name, if any.
    String[] aliases = definitionHolder.getAliases();
    if (aliases != null) {
        for (String alias : aliases) {
            registry.registerAlias(beanName, alias);
        }
    }
}
```

## 5、Bean 加载

```java

```





### 5.1、FactoryBean

> 1、用于生产单独的 bean。传统的 bean 从 beanFactory中生产，需要指定各种属性，比较负责。采用实现 FactoryBean 的方式比较简单



#### FactoryBean

```java
@Component("user")
public class UserFactoryBean implements FactoryBean<User> {

    @Override
    public User getObject() throws Exception {
        return new User("rose");
    }

    @Override
    public Class<?> getObjectType() {
        return User.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}
```

#### spring-config.xml

```xml
<bean id="user" class="com.bigguy.spring.entity.User" >
    <property name="username" value="tom" />
</bean>

<bean id="userFactory" class="com.bigguy.spring.service.UserFactoryBean" >
</bean>
```

#### 测试

> 1、实现FactoryBean 接口的 bean 可以直接获取工厂生产的实例
>
> 2、当需要获得该 FactoryBean 工厂实例时，需要在beanName 前面加"&"。不加的话表示获取工厂生产的 bean

```java
public static void main(String[] args) {
    BeanFactory beanFactory =  new XmlBeanFactory(new ClassPathResource(SPRING_CONFIG_PATH));
    User user = beanFactory.getBean( "userFactory", User.class);

    UserFactoryBean userFactoryBean = beanFactory.getBean("&userFactory", UserFactoryBean.class);
    User user2 = userFactoryBean.getObject();

    System.out.println(user);
    // 两个实例是同一个
    System.out.println(user.equals(user2));
}
```



## 错误处理

ReadContext

```java
/**
* Raise a regular error.
*/
public void error(String message, Object source, ParseState parseState, Throwable cause) {
    Location location = new Location(getResource(), source);
    this.problemReporter.error(new Problem(message, location, parseState, cause));
}
```







```java
public class FailFastProblemReporter implements ProblemReporter {

	private Log logger = LogFactory.getLog(getClass());

	public void setLogger(Log logger) {
		this.logger = (logger != null ? logger : LogFactory.getLog(getClass()));
	}

	@Override
	public void fatal(Problem problem) {
		throw new BeanDefinitionParsingException(problem);
	}

	
	@Override
	public void error(Problem problem) {
		throw new BeanDefinitionParsingException(problem);
	}

	@Override
	public void warning(Problem problem) {
		this.logger.warn(problem, problem.getRootCause());
	}

}

```







































o