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