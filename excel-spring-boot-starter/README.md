# Excel 导入导出 Spring Boot Starter

[![Maven Central](https://img.shields.io/maven-central/v/io.github.archer099/excel-spring-boot-starter.svg?style=flat-square)](https://search.maven.org/artifact/io.github.archer099/excel-spring-boot-starter)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg?style=flat-square)](http://www.apache.org/licenses/LICENSE-2.0.html)

**一句话概述：** 基于FastExcel的高性能Excel处理框架，支持大数据量导入导出，多Sheet处理，提供简单易用的接口和多种实现方式。

## 背景

在企业应用开发中，Excel导入导出是一个常见的需求，尤其是在数据报表、批量数据处理等场景。传统的Excel处理方式在处理大数据量时常常面临内存溢出、性能瓶颈等问题。

`excel-spring-boot-starter` 基于[FastExcel](https://github.com/wangfeng-coder/fast-excel)核心，提供了更高性能、更灵活的Excel处理解决方案，支持大数据量导入导出、多线程处理、多Sheet拆分等高级功能，同时保持了简单易用的API风格。

## 项目目标

- **高性能**：基于底层高性能的FastExcel，优化大数据量处理
- **易扩展**：提供多种抽象类和接口，方便自定义扩展
- **多场景支持**：覆盖常见的Excel导入导出场景
- **线程安全**：多线程并发处理，提高大数据量处理效率
- **内存优化**：分批处理数据，避免OOM问题

## 核心功能与亮点 ✨

- **简单Excel处理**：适用于中小数据量的简单导入导出
- **大数据量处理**：支持百万级数据导入导出（自动拆分为多文件压缩包）
- **多Sheet支持**：单Excel文件多Sheet页导出，适合复杂报表
- **异步处理**：后台任务处理大数据量，避免请求超时
- **多线程并发**：导入导出过程利用多线程提高性能
- **分批处理**：数据分批读取写入，降低内存占用
- **易用API**：简洁的API设计，减少样板代码

## 技术栈 🛠️

- Java 21
- Spring Boot 3.x
- FastExcel/EasyExcel
- Hutool

## 快速开始 🚀

### 添加依赖

```xml
<dependency>
    <groupId>io.github.archer099</groupId>
    <artifactId>excel-spring-boot-starter</artifactId>
    <version>${latest.version}</version>
</dependency>
```

### 简单导出示例

最基础的Excel导出方式，适用于数据量较小的场景：

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description Excel工具类使用示例
 */
@RestController
@RequestMapping("/excel")
public class SimpleExcelController {

    /**
     * 简单导出Excel
     */
    @GetMapping("/export")
    public void exportUsers(HttpServletResponse response) throws IOException {
        // 准备数据
        List<UserDTO> users = List.of(
            new UserDTO(1L, "张三", "男", 25),
            new UserDTO(2L, "李四", "女", 30),
            new UserDTO(3L, "王五", "男", 28)
        );
        
        // 导出Excel
        ExcelUtil.exportToExcel(users, "用户列表", response, UserDTO.class);
    }
    
    /**
     * 简单导入Excel
     */
    @PostMapping("/import")
    public String importUsers(@RequestParam("file") MultipartFile file) throws IOException {
        AtomicInteger success = new AtomicInteger();
        
        // 导入Excel
        ExcelUtil.importFromExcel(file, UserDTO.class, new PageReadListener<UserDTO>(
            data -> {
                // 处理导入数据
                success.addAndGet(data.size());
                log.info("导入数据：{}", data);
            }
        ));
        
        return "成功导入 " + success.get() + " 条数据";
    }
}

/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description 用户数据传输对象
 */
@Data
public class UserDTO {
    @ExcelProperty("用户ID")
    private Long id;
    
    @ExcelProperty("姓名")
    private String name;
    
    @ExcelProperty("性别")
    private String gender;
    
    @ExcelProperty("年龄")
    private Integer age;
    
    // 构造方法省略...
}
```

### 大数据量导出示例

处理大量数据时，可以使用`LargeExcelZipExporter`将数据拆分成多个Excel文件并打包为Zip：

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description 大数据量Excel导出实现类
 */
@Service
@RequiredArgsConstructor
public class UserLargeExcelExporter extends LargeExcelZipExporter<UserDTO, UserQueryParam> {
    
    private final UserService userService;
    
    /**
     * 构造方法，注入线程池
     */
    public UserLargeExcelExporter(@Qualifier("excelThreadPool") ExecutorService executorService) {
        super(executorService);
    }
    
    /**
     * 获取导出数据
     */
    @Override
    public List<UserDTO> getExportData(LargeExcelZipExportContext<UserDTO, UserQueryParam> context) {
        UserQueryParam param = context.getParam();
        int page = context.getCurrentPage();
        int size = context.getPageSize();
        
        // 从数据库分页查询数据
        return userService.queryUserList(param, page, size);
    }
    
    /**
     * 获取总数据量
     */
    @Override
    public Long getTotalCount(LargeExcelZipExportContext<UserDTO, UserQueryParam> context) {
        return userService.countUsers(context.getParam());
    }
}

/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description Excel控制器
 */
@RestController
@RequestMapping("/excel")
@RequiredArgsConstructor
public class LargeExcelController {
    
    private final UserLargeExcelExporter userLargeExcelExporter;
    private final UserService userService;
    
    /**
     * 大数据量导出
     */
    @GetMapping("/large-export")
    public void exportLargeExcel(UserQueryParam param, HttpServletResponse response) throws Exception {
        // 创建导出上下文
        LargeExcelZipExportContext<UserDTO, UserQueryParam> context = new LargeExcelZipExportContext<>();
        context.setEntityClass(UserDTO.class);
        context.setFileName("用户数据导出");
        context.setSheetName("用户列表");
        context.setParam(param);
        context.setPageSize(1000); // 每页1000条
        context.setMaxRowsPerSheet(500000); // 每个Excel文件最多50万行
        context.setFetchSize(1000); // 每次查询1000条
        context.setTempDir(System.getProperty("java.io.tmpdir")); // 临时目录
        context.setUniqueId(UUID.randomUUID().toString()); // 唯一标识
        
        // 执行导出
        userLargeExcelExporter.exportLargeExcelToZip(context, response);
    }
}
```

### 多Sheet导出示例

当需要在一个Excel中导出多个相关数据的Sheet页：

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description 多Sheet页Excel导出实现
 */
@Service
@RequiredArgsConstructor
public class ReportMultiSheetExporter extends MultiSheetExcelExporter<Object, ReportQueryParam> {

    private final ReportService reportService;
    
    public ReportMultiSheetExporter(@Qualifier("excelThreadPool") ExecutorService executorService) {
        super(executorService);
    }
    
    @Override
    public List<Object> getExportData(MultiSheetExportContext<Object, ReportQueryParam> context) {
        int pageNo = context.getCurrentPage();
        int pageSize = context.getPageSize();
        ReportQueryParam param = context.getParam();
        
        // 根据不同的Sheet获取不同的数据
        return switch (context.getSheetIndex()) {
            case 1 -> reportService.getSalesData(param, pageNo, pageSize);
            case 2 -> reportService.getInventoryData(param, pageNo, pageSize);
            case 3 -> reportService.getFinanceData(param, pageNo, pageSize);
            default -> Collections.emptyList();
        };
    }
    
    @Override
    public Long getTotalCount(MultiSheetExportContext<Object, ReportQueryParam> context) {
        ReportQueryParam param = context.getParam();
        
        // 根据不同Sheet返回不同的总数
        return switch (context.getSheetIndex()) {
            case 1 -> reportService.countSalesData(param);
            case 2 -> reportService.countInventoryData(param);
            case 3 -> reportService.countFinanceData(param);
            default -> 0L;
        };
    }
}

/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description 报表控制器
 */
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportMultiSheetExporter reportExporter;
    
    /**
     * 导出多Sheet页报表
     */
    @GetMapping("/export")
    public void exportReport(ReportQueryParam param, HttpServletResponse response) throws Exception {
        // 创建导出上下文
        MultiSheetExportContext<Object, ReportQueryParam> context = new MultiSheetExportContext<>();
        context.setFileName("综合报表");
        context.setSheetName("报表");
        context.setParam(param);
        context.setEntityClass(Object.class); // 实际使用时应根据不同Sheet指定不同类
        context.setPageSize(500);
        context.setMaxRowsPerSheet(100000);
        context.setSheetIndex(1); // 起始Sheet索引
        
        // 执行多Sheet导出
        reportExporter.exportMultiSheetExcel(context, response);
    }
}
```

### 大数据量导入示例

处理大量数据导入，使用多线程并发处理：

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description 大数据量Excel导入实现
 */
@Service
@RequiredArgsConstructor
public class ProductImporter extends LargeDataExcelImporter<ProductDTO> {

    private final ProductService productService;
    
    public ProductImporter(@Qualifier("excelThreadPool") ExecutorService executorService) {
        super(executorService);
    }
    
    @Override
    public void handleImportData(List<ProductDTO> dataList, LargeDataImportContext<ProductDTO> context) {
        // 批量处理导入数据
        productService.batchSaveProducts(dataList);
    }
}

/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description 商品导入控制器
 */
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductImporter productImporter;
    
    /**
     * 大数据量商品导入
     */
    @PostMapping("/import")
    public String importProducts(@RequestParam("file") MultipartFile file) throws Exception {
        // 创建导入上下文
        LargeDataImportContext<ProductDTO> context = new LargeDataImportContext<>();
        context.setFile(file);
        context.setEntityClass(ProductDTO.class);
        context.setBatchSize(1000); // 每批1000条
        context.setThreadCount(5); // 使用5个线程处理
        context.setQueueSize(20); // 队列大小
        context.setContinueOnError(true); // 错误时继续处理
        context.setSheetNo(0); // 第一个Sheet页
        
        // 设置进度回调（可选）
        context.setProgressCallback((total, expected, success, failed) -> {
            log.info("导入进度: 已处理{}, 成功{}, 失败{}", total, success, failed);
        });
        
        // 执行导入
        productImporter.importLargeExcel(context);
        
        return "导入处理已启动，请等待完成";
    }
}
```

## 自定义处理器

您可以通过继承框架提供的抽象类，实现自定义的Excel处理逻辑：

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description 自定义Excel处理器
 */
@Service
public class CustomExcelProcessor extends AbstractSimpleExcelProcessor<CustomData, CustomQuery> {

    private final CustomDataService dataService;
    
    public CustomExcelProcessor(@Qualifier("excelThreadPool") ExecutorService executorService,
                              CustomDataService dataService) {
        super(executorService);
        this.dataService = dataService;
    }
    
    @Override
    public void handleImportData(List<CustomData> dataList, ImportContext context) {
        // 自定义导入处理逻辑
        dataService.batchProcess(dataList);
    }
    
    @Override
    public List<CustomData> getExportData(ExportContext<CustomData, CustomQuery> context) {
        // 自定义导出数据获取逻辑
        return dataService.queryData(context.getParam());
    }
}
```

## 配置选项

在 `application.yml` 中可以配置线程池参数：

```yaml
excel:
  thread-pool:
    core-size: 10
    max-size: 20
    queue-capacity: 500
    keep-alive-seconds: 60
```

## 最佳实践

1. **大数据量处理**
   - 使用`LargeExcelZipExporter`和`LargeDataExcelImporter`进行大数据量处理
   - 设置合理的批处理大小，一般500-1000条比较合适
   - 根据服务器资源调整线程数，通常不建议超过CPU核心数的2倍

2. **内存优化**
   - 导出大数据量时，将数据分为多个Excel文件，打包为ZIP文件下载
   - 导入时，使用分批次读取和处理，避免一次性加载全部数据
   - 使用流式读写API，避免在内存中缓存整个Excel文件

3. **性能调优**
   - 合理设置每批数据量，根据实际场景调整
   - 预先计算总数据量，以便更好地规划内存和线程使用
   - 对于特别大的文件，考虑使用异步处理，通过消息队列或异步任务

4. **异常处理**
   - 实现细粒度的错误处理策略，错误时可选择继续处理或终止
   - 记录导入失败的数据，以便后续修复或重试
   - 考虑使用事务管理，确保数据一致性

## 常见问题

1. **导出Excel为什么文件会被损坏？**
   - 可能是因为导出过程中发生异常，但HTTP响应已经部分写入
   - 确保导出过程中不会抛出未捕获的异常
   - 检查响应头设置是否正确

2. **如何处理复杂的Excel格式？**
   - 可以扩展`ExcelWriter`，自定义样式处理器
   - 使用底层FastExcel/EasyExcel的高级功能
   - 对于非常复杂的格式，考虑使用模板方式

3. **导入大文件时内存溢出？**
   - 减小每批处理的数据量
   - 增加JVM堆内存
   - 检查是否有内存泄漏，如某些对象没有被及时释放

## 高级特性

1. **动态表头**

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description 动态表头处理
 */
@Component
public class DynamicHeaderExporter {

    public void exportWithDynamicHeaders(List<Map<String, Object>> data, 
                                        List<String> headers,
                                        HttpServletResponse response) throws IOException {
        // 使用FastExcel底层API实现动态表头
        // ...实现代码省略
    }
}
```

2. **Excel模板填充**

```java
/**
 * @author archer099
 * @date 2025-02-13 14:31:00
 * @description Excel模板填充
 */
@Component
public class ExcelTemplateFiller {

    public void fillTemplate(String templatePath, 
                           Map<String, Object> data, 
                           HttpServletResponse response) throws IOException {
        // 使用模板填充Excel
        // ...实现代码省略
    }
}
```

## 应用场景

- **报表导出**：财务报表、统计报表等各类报表导出
- **批量数据导入**：商品数据、用户数据等批量导入
- **数据迁移**：系统迁移时的数据导出导入
- **数据备份**：将数据导出为Excel进行备份
- **模板下载与数据收集**：提供Excel模板下载，用户填写后上传

## 贡献 🙏

欢迎提交Issue或Pull Request参与项目贡献！

## 许可证

本项目使用 [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0.html) 许可证。 