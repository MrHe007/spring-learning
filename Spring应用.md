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

### 5.1、流程

#### a、转换 beanName

- 传入的 beanName 可能是factoryBean，或者是 bean 的别名。需要转换成最终的 beanName
- factoryBean：如果是"&"开头，表示是取 factoryBean 本身，而不是其生产的bean
- beanAlias：A -> B -> C ,最终取C

```java
protected String transformedBeanName(String name) {
    return canonicalName(BeanFactoryUtils.transformedBeanName(name));
}
// 转换 beanName，去除 &
public static String transformedBeanName(String name) {
    String beanName = name;
    while (beanName.startsWith(BeanFactory.FACTORY_BEAN_PREFIX)) {
        beanName = beanName.substring(BeanFactory.FACTORY_BEAN_PREFIX.length());
    }
    return beanName;
}
// 将别名转换为beanName，取最终的 beanName
public String canonicalName(String name) {
    String canonicalName = name;
    // Handle aliasing...
    String resolvedName;
    do {
        resolvedName = this.aliasMap.get(canonicalName);
        if (resolvedName != null) {
            canonicalName = resolvedName;
        }
    }while (resolvedName != null);
    return canonicalName;
}
```

#### b、尝试从单例缓存中取该bean

[Spring解决循环依赖](https://www.cnblogs.com/jajian/p/10241932.html)

DefaultSingletonBeanRegistry

```java
/** Cache of singleton objects: bean name --> bean instance */
private final Map<String, Object> singletonObjects = new ConcurrentHashMap<String, Object>(256);


// 尝试从缓存中取 bean
protected Object getSingleton(String beanName, boolean allowEarlyReference) {
    Object singletonObject = this.singletonObjects.get(beanName);
    if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {
        synchronized (this.singletonObjects) {
            singletonObject = this.earlySingletonObjects.get(beanName);
            if (singletonObject == null && allowEarlyReference) {
                ObjectFactory<?> singletonFactory = this.singletonFactories.get(beanName);
                if (singletonFactory != null) {
                    singletonObject = singletonFactory.getObject();
                    this.earlySingletonObjects.put(beanName, singletonObject);
                    this.singletonFactories.remove(beanName);
                }
            }
        }
    }
    return (singletonObject != NULL_OBJECT ? singletonObject : null);
}

```







### 5.2、bean 加载过程中细节

#### 5.2.1、FactoryBean

> 1、用于生产单独的 bean。传统的 bean 从 beanFactory中生产，需要指定各种属性，比较负责。采用实现 FactoryBean 的方式比较简单

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

##### spring-config.xml

```xml
<bean id="user" class="com.bigguy.spring.entity.User" >
    <property name="username" value="tom" />
</bean>

<bean id="userFactory" class="com.bigguy.spring.service.UserFactoryBean" >
</bean>
```

##### 测试

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









#### 5.2.2、缓存中获取单例 bean

1. 单例在 Spring 的容器中只会被创建一次，后续在获取的时候，直接从单例缓存中获取。
2. 上面获取也只是在缓存中尝试获取。如果没有取到会从 singletonFactories 中加载
3. Spring 创建 bean 的原则是不等 bean “完全”创建完成就会将创建bean 的 ObjectFactory提早曝光到缓存中
4. 一旦下个bean 创建需要依赖上个bean，直接使用 ObjectFactory

> ```java
> /** Cache of early singleton objects: bean name --> bean instance */
> private final Map<String, Object> earlySingletonObjects = new HashMap<String, Object>(16);
> ```
>
> earlySingletonObjects：
>
> 	- 将正在加载的 bean放在该容器中
> 	- 用于保存 bean 的引用，解决 field，和 setter 的循环依赖问题。
> 	- 保存的bean 已经实例化了，但是没有进行属性注入...等其他后续操作



- singletonObjects：保存最终完整的 bean（实例化，属性注入...）
- earlySingletonObjects：保存正在加载的 bean（此时的 bean 不是完整的bean。可能实例化了，但是没有属性注入等其他操作）
- singletonFactories：beanName 和 bean工厂之间的关系



```java
protected Object getSingleton(String beanName, boolean allowEarlyReference) {
    	// 检查缓存是否存在实例
		Object singletonObject = this.singletonObjects.get(beanName);
		if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {
			synchronized (this.singletonObjects) {
                // 如果该 bean 正在加载，则不作处理。正在处理时：singletonObject 不为空
				singletonObject = this.earlySingletonObjects.get(beanName);
				if (singletonObject == null && allowEarlyReference) {
           // 当某些方法需要提前初始化时(AOP操作)，会调用 addSingletonFactory 方法将对于的 ObjectFactory "初始化策略"存储在  singletonFactories 中
					ObjectFactory<?> singletonFactory = this.singletonFactories.get(beanName);
					if (singletonFactory != null) {
                        // 调用预先设定的 getObject 方法
						singletonObject = singletonFactory.getObject();
                        // 记录缓存：earlySingletonObjects 和 singletonFactories 互斥
						this.earlySingletonObjects.put(beanName, singletonObject);
						this.singletonFactories.remove(beanName);
					}
				}
			}
		}
		return (singletonObject != NULL_OBJECT ? singletonObject : null);
	}
```

#### 5.2.3、从 bean 实例中获取对象

> 无论是从缓存中获取对象还是 

得到 bean实例后我们需要校验该bean是不是FactoryBean，如果是FactoryBean的话，需要调用该对象的 getObject 方法作为返回值

AbstractBeanFactory

```java

Object sharedInstance = getSingleton(beanName);
if (sharedInstance != null && args == null) {
    if (logger.isDebugEnabled()) {
        if (isSingletonCurrentlyInCreation(beanName)) {
            logger.debug("Returning eagerly cached instance of singleton bean '" + beanName +
                         "' that is not fully initialized yet - a consequence of a circular reference");
        }
        else {
            logger.debug("Returning cached instance of singleton bean '" + beanName + "'");
        }
    }
    bean = getObjectForBeanInstance(sharedInstance, name, beanName, null);
}
// ....

protected Object getObjectForBeanInstance(
    Object beanInstance, String name, String beanName, RootBeanDefinition mbd) {
    // 如果是 FactoryBean 类型的bean，该bean 一定是实现了 FactoryBean接口的
    if (BeanFactoryUtils.isFactoryDereference(name) && !(beanInstance instanceof FactoryBean)) {
        throw new BeanIsNotAFactoryException(transformedBeanName(name), beanInstance.getClass());
    }
    // 如果是普通的 bean，直接返回
    if (!(beanInstance instanceof FactoryBean) || BeanFactoryUtils.isFactoryDereference(name)) {
        return beanInstance;
    }
    // 下面是针对factoryBean的判断规则
    Object object = null;
    if (mbd == null) {
        // 尝试从缓存中加载 factoryBean 返回的bean。这种factoryBean生产bean的方式也是单例的
        object = getCachedObjectForFactoryBean(beanName);
    }
    if (object == null) {
        // 转换成factoryBean
        FactoryBean<?> factory = (FactoryBean<?>) beanInstance;
        // Caches object obtained from FactoryBean if it is a singleton.
        if (mbd == null && containsBeanDefinition(beanName)) {
            mbd = getMergedLocalBeanDefinition(beanName);
        }
        boolean synthetic = (mbd != null && mbd.isSynthetic());
        // 从 factoryBean中获取 bean
        object = getObjectFromFactoryBean(factory, beanName, !synthetic);
    }
    return object;
}
/**  从 factoryBean中获取 bean
	1、对单例，多例区分处理
	2、单例：获取后存到缓存中。多例：不存缓存。每次取到的都是新的对象。
	3、后续 postProcess.... 操作
*/
protected Object getObjectFromFactoryBean(FactoryBean<?> factory, String beanName, boolean shouldPostProcess) {
    // 判断该 factory 是否是单例的
    if (factory.isSingleton() && containsSingleton(beanName)) {
        synchronized (getSingletonMutex()) {
            Object object = this.factoryBeanObjectCache.get(beanName);
            if (object == null) {
                // 真正获取 bean
                object = doGetObjectFromFactoryBean(factory, beanName);

                if (object != null && shouldPostProcess) {
                    try {
                        object = postProcessObjectFromFactoryBean(object, beanName);
                    }
                    //...
                }
                // 获取到 bean后，放到缓存中
                this.factoryBeanObjectCache.put(beanName, (object != null ? object : NULL_OBJECT));
            }
            return (object != NULL_OBJECT ? object : null);
        }
    }
    else {
        // 多例
        Object object = doGetObjectFromFactoryBean(factory, beanName);
        if (object != null && shouldPostProcess) {
            try {
                // 执行 postProcessObject....
                object = postProcessObjectFromFactoryBean(object, beanName);
            }
           //...
        }
        return object;
    }
}

```









#### 5.2.2、解决循环依赖

[参考博客](https://www.cnblogs.com/jajian/p/10241932.html) [参考博客2](https://xie.infoq.cn/article/e3b46dc2c0125ab812f9aa977)

循环依赖：在 bean 实例过程中，引用其它 bean（构造器，属性、方法中），造成循环依赖

![img](https://img2018.cnblogs.com/blog/1162587/201901/1162587-20190108224120891-308387799.png)



循环依赖主要存在三种情况：

1. 构造器中：出现循环依赖会报错
2. 属性、方法中：出现循环依赖不会报错
3. scope=prototype 类型的 bean：出现循环依赖会报错



为什么在当其它的 bean 作为属性或者方法中的参数进行注入时不会报错呢？

原因是 Spring 对 bean 解析的流程上。

![img](https://img2018.cnblogs.com/blog/1162587/201901/1162587-20190108223107433-1167042193.png)



可以大致将 spring 对 bean 加载分为三个步骤：

1. 实例化bean对象。
2. 设置对象属性
3. 后置处理

##### 构造器参数循环依赖

1. Spring容器会将每一个正在创建的 Bean 标识符放在一个**“当前创建Bean池”**中（singletonsCurrentlyInCreation）

```java
// 注意是一个 set 集合
private final Set<String> singletonsCurrentlyInCreation = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>(16));
```

2. Bean标识符在创建过程中将一直保持在这个池中，因此如果在创建Bean过程中发现自己已经在“当前创建Bean池”里时将抛出`BeanCurrentlyInCreationException`异常表示循环依赖
3. 而对于创建完毕的Bean将从“当前创建Bean池”中清除掉。



上方案例解析：

1. Spring容器先创建单例StudentA，StudentA依赖StudentB，然后将A放在**“当前创建Bean池”**中，此时创建 StudentB，StudentB 依赖 StudentC ，然后将B放在**“当前创建Bean池”**中
2. 此时创建StudentC，StudentC又依赖StudentA， 但是，此时StudentA已经在池中，所以会报错
3. 因为在池中的Bean都是未初始化完的，所以会依赖错误 （初始化完的Bean会从池中移除）。



https://xie.infoq.cn/article/e3b46dc2c0125ab812f9aa977

```JAVA
public Object getSingleton(String beanName) {
   return getSingleton(beanName, true);
}

protected Object getSingleton(String beanName, boolean allowEarlyReference) {
   //首先去一级缓存中获取如果获取的到说明bean已经存在，直接返回
   Object singletonObject = this.singletonObjects.get(beanName);
   //如果一级缓存中不存在，则去判断该bean是否在创建中，如果该bean正在创建中，就说明了，这个时候发生了循环依赖
   if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {
      synchronized (this.singletonObjects) {
         //如果发生循环依赖，首先去二级缓存中获取，如果获取到则返回，这个地方就是获取aop增强以后的bean
         singletonObject = this.earlySingletonObjects.get(beanName);
         //如果二级缓存中不存在，且允许提前访问三级引用
         if (singletonObject == null && allowEarlyReference) {
            //去三级缓存中获取
            ObjectFactory<?> singletonFactory = this.singletonFactories.get(beanName);
            if (singletonFactory != null) {
               //如果三级缓存中的lambda表达式存在，执行aop，获取增强以后的对象，为了防止重复aop，将三级缓存删除，升级到二级缓存中
               singletonObject = singletonFactory.getObject();
               this.earlySingletonObjects.put(beanName, singletonObject);
               this.singletonFactories.remove(beanName);
            }
         }
      }
   }
   return singletonObject;
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