
### **🔄 FlowStep 框架設計文檔 (最終完整版)**
#### **基於 Spring Boot 的「查-改分離」框架架構**
#### **支援 Query (R) 與 Command (CUD) 的統一設計語言**

---

### **🎯 目標**

建立一套 統一、可擴展、強制遵循流程、高可測試性、易維護 的企業級服務開發規範，明確區分「查詢」與「寫入」操作，實現：

| 目標 | 實現方式 |
| :--- | :--- |
| ✅ 查改分離 | 區分 `QueryTemplate` 與 `CommandTemplate` |
| ✅ 流程強制 | 模板方法模式，定義骨架流程 |
| ✅ 結果純淨 | 返回純業務物件，不綁定 HTTP 層 |
| ✅ 上下文共享 | `BaseContext` 支援跨步驟數據傳遞 |
| ✅ 易於擴展 | 新增服務只需繼承模板並實現抽象方法 |
| ✅ 高可測試性 | 每個 Step 可獨立單元測試，支援 ArchUnit 稽核 |
| ✅ 架構可治理 | 自定義註解 + 靜態分析，防止架構腐蝕 |
| ✅ 開箱即用 | `validate` 提供默認實現，簡單場景無需重寫 |
| ✅ 條件直觀 | 在 `steps()` 方法中直接使用 `if-else`，清晰易懂 |

---

### **📁 整體項目結構**

```
src/
 └── main/
    └── java/
        └── com.example.demo/
            ├── DemoApplication.java
            │
            └── template/                      ← 核心元件共用層
                ├── annotation/               ← 自定義註解
                │   ├── QueryFlow.java        ← 標記查詢服務
                │   └── CommandFlow.java      ← 標記寫入服務
                │
                ├── context/                  ← 上下文基類
                │   └── BaseContext.java      ← 共用存儲機制
                │   ├── QueryContext.java   ← 查詢專屬上下文
                │   └── CommandContext.java   ← 寫入專屬上下文
                │
                ├── step/                     ← 步驟接口
                │   ├── QueryStep.java      ← 查詢步驟
                │   ├── CommandStep.java      ← 寫入步驟
                │   └── StepResult.java       ← 步驟結果封裝
                │
                ├── exception/                ← 共用異常
                │   ├── BusinessException.java ← 業務異常模型
                │   └── ErrorType.java        ← 公共錯誤類型枚舉
                │
                ├── QueryTemplate.java      ← 查詢模板
                └── CommandTemplate.java      ← 寫入模板
            │
            ├── domain/
            │   ├── request/                  ← 查詢請求
            │   │   └── QueryRequest.java
            │   ├── command/                  ← 寫入命令
            │   │   └── CreateUserCommand.java
            │   ├── response/                 ← 統一響應模型
            │   │   ├── QueryResponse.java
            │   │   └── CreateResponse.java
            │   └── User.java
            │
            ├── repository/
            │   └── UserMapper.java
            │
            ├── client/
            │   └── ExternalApiClient.java
            │
            ├── validator/
            │   ├── QueryValidator.java     ← 查詢驗證
            │   └── CommandValidator.java     ← 寫入驗證
            │
            ├── service/impl/
            │   ├── CreditQueryService.java ← 查詢服務示例
            │   └── CreateUserService.java    ← 寫入服務示例
            │
            └── controller/
                ├── QueryController.java
                └── CommandController.java
```

> 💡 **說明**：`template` 包為核心基礎設施，所有服務皆由此衍生，確保設計語言統一。

---

### **🔧 核心設計元件（統一設計語言）**

#### **1. `@QueryFlow` 與 `@CommandFlow` —— 服務標記與稽核基石**

```java
// 查詢服務標記
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface QueryFlow {
    String code();
    String desc();
}

// 寫入服務標記
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CommandFlow {
    String code();
    String desc();
}
```

✅ **用途**：
*   標識系統中所有標準化服務。
*   支援 ArchUnit 靜態稽核，強制所有 enquiry/command 服務必須標註對應註解。
*   為未來實現「服務註冊中心」、「流程可視化」、「操作審計」提供元數據支持。

---

#### **2. `ErrorType` —— 公共錯誤類型枚舉**

```java
// 文件位置: template/exception/ErrorType.java
package com.example.demo.template.exception;

/**
 * 錯誤類型枚舉，用於統一標識錯誤的性質。
 * 適用於 StepResult 和 BusinessException。
 */
public enum ErrorType {
    /**
     * 輸入驗證錯誤（如參數缺失、格式錯誤）
     * 通常由客戶端修復。
     */
    VALIDATION,

    /**
     * 業務邏輯錯誤（如餘額不足、庫存不足）
     * 通常由業務規則或用戶決策解決。
     */
    BUSINESS,

    /**
     * 系統內部錯誤（如數據庫連接失敗、空指針）
     * 需要開發人員介入修復。
     */
    SYSTEM
}
```

✅ **用途**：
*   作為 `StepResult` 和 `BusinessException` 的共享錯誤分類標準。
*   為全局異常處理器 (`@ControllerAdvice`) 提供自動化 HTTP 狀態碼映射的依據。
*   支持未來的監控告警系統按錯誤類型進行聚合分析。

---

#### **3. `BaseContext` —— 上下文共享基類（共用）**

```java
public abstract class BaseContext {
    protected final Map<String, Object> store = new HashMap<>();

    public <T> void put(String key, T value) { store.put(key, value); }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) { return (T) store.get(key); }

    public boolean has(String key) { return store.containsKey(key); }

    // 新增：帶默認值的獲取方法，提升條件判斷的流暢性
    @SuppressWarnings("unchecked")
    public <T> T getOrDefault(String key, T defaultValue) {
        T value = (T) store.get(key);
        return value != null ? value : defaultValue;
    }
}
```

✅ **特點**：簡潔、靈活、無侵入，為多步驟間傳遞中間結果提供統一容器。

---

#### **4. `QueryContext` / `CommandContext` —— 專屬上下文（繼承擴展）**

```java
@Data
@EqualsAndHashCode(callSuper=false)
public class QueryContext extends BaseContext {
    // 可擴展：追加查詢場景專屬欄位（如 traceId）
}

@Data
@EqualsAndHashCode(callSuper=false)
public class CommandContext extends BaseContext {
    // 可擴展：追加 command 專屬欄位
}
```

✅ **設計原則**：繼承 `BaseContext`，保留核心能力，並根據場景擴展語義屬性。

---

#### **5. `StepResult<T>` —— 步驟執行結果封裝（共用）**

```java
public class StepResult<T> {
    private final boolean success;
    private final T data;
    private final String message;
    private final String errorCode;
    private final ErrorType errorType; // <-- 引用公共 ErrorType

    // 移除內部的 enum ErrorType 定義

    // 靜態工廠方法
    public static <T> StepResult<T> success(T data) {
        return new StepResult<>(true, data, null, null, null);
    }

    public static <T> StepResult<T> failure(String message, String errorCode, ErrorType type) {
        return new StepResult<>(false, null, message, errorCode, type);
    }

    // 最簡化版本，方便快速原型開發
    public static <T> StepResult<T> failure(String message) {
        return failure(message, "GENERIC_ERROR", ErrorType.BUSINESS);
    }

    // 私有構造器...
    private StepResult(boolean success, T data, String message, String errorCode, ErrorType errorType) {
        this.success = success;
        this.data = data;
        this.message = message;
        this.errorCode = errorCode;
        this.errorType = errorType;
    }

    // getter...
    public boolean isSuccess() { return success; }
    public T getData() { return data; }
    public String getMessage() { return message; }
    public String getErrorCode() { return errorCode; }
    public ErrorType getErrorType() { return errorType; }
}
```

✅ **價值**：統一結果格式，便於流程判斷與異常轉譯。

---

#### **6. `BusinessException` —— 業務異常模型（共用）**

```java
public class BusinessException extends Exception {
    private final String errorCode;
    private final ErrorType errorType; // <-- 引用公共 ErrorType

    // 移除內部的 enum ErrorType 定義

    public BusinessException(String errorCode, String message, ErrorType errorType) {
        super(message);
        this.errorCode = errorCode;
        this.errorType = errorType;
    }

    // getter...
    public String getErrorCode() { return errorCode; }
    public ErrorType getErrorType() { return errorType; }
}
```

✅ **優勢**：語義清晰，利於日誌分析、監控告警與前端處理。

---

#### **7. `QueryStep<T>` / `CommandStep<T>` —— 可插拔的函數式步驟（對稱設計）**

```java
@FunctionalInterface
public interface QueryStep<T> {
    StepResult<T> execute(QueryContext context) throws Exception;
}

@FunctionalInterface
public interface CommandStep<T> {
    StepResult<T> execute(CommandContext context) throws Exception;
}
```

✅ **能力**：支援 DB 查詢、外部 API 調用、條件判斷、事件發送等，高度模組化，可複用。

---

#### **8. `QueryTemplate<R, S>` —— 查詢流程骨架**

```java
@Slf4j
public abstract class QueryTemplate<R, S> {

    public final S execute(R request) throws BusinessException {
        try {
            // 1. 驗證 (使用默認實現或子類重寫)
            StepResult<Void> validate = validate(request);
            if (!validate.isSuccess()) {
                throw new BusinessException(validate.getErrorCode(), validate.getMessage(), validate.getErrorType());
            }

            // 2. 建立上下文
            QueryContext context = new QueryContext();
            context.put("request", request);

            // 3. 執行步驟（串行）
            for (QueryStep<?> step : steps(request, context)) { // <-- 傳入 request 和 context
                @SuppressWarnings("unchecked")
                StepResult<Object> result = ((QueryStep<Object>) step).execute(context);
                if (!result.isSuccess()) {
                    throw new BusinessException(result.getErrorCode(), result.getMessage(), result.getErrorType());
                }
            }

            // 4. 封裝並返回純業務結果
            return buildResponse(context);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error in enquiry", e);
            throw new BusinessException("SYS_001", "系統錯誤", ErrorType.SYSTEM);
        }
    }

    // 提供默認實現，簡單服務可省略重寫
    protected StepResult<Void> validate(R request) {
        return StepResult.success(null);
    }

    // 修改簽名，傳入 request 和 context，方便條件判斷
    protected abstract List<QueryStep<?>> steps(R request, QueryContext context);

    protected abstract S buildResponse(QueryContext context);
}
```

✅ **關鍵**：無事務、無副作用，返回純粹查詢結果。

---

#### **9. `CommandTemplate<C, R>` —— 寫入流程骨架（含事務與審計）**

```java
@Slf4j
public abstract class CommandTemplate<C, R> {

    public final R execute(C command) throws BusinessException {
        try {
            // 1. 驗證命令 (使用默認實現或子類重寫)
            StepResult<Void> validate = validate(command);
            if (!validate.isSuccess()) {
                throw new BusinessException(validate.getErrorCode(), validate.getMessage(), validate.getErrorType());
            }

            // 2. 建立上下文
            CommandContext context = new CommandContext();
            context.put("command", command);

            // 3. 執行所有步驟（串行）
            for (CommandStep<?> step : steps(command, context)) { // <-- 傳入 command 和 context
                @SuppressWarnings("unchecked")
                StepResult<Object> result = ((CommandStep<Object>) step).execute(context);
                if (!result.isSuccess()) {
                    throw new BusinessException(result.getErrorCode(), result.getMessage(), result.getErrorType());
                }
            }

            // 4. 封裝並返回業務結果
            return buildResponse(context);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error in command execution", e);
            throw new BusinessException("SYS_001", "系統錯誤", ErrorType.SYSTEM);
        }
    }

    // 提供默認實現
    protected StepResult<Void> validate(C command) {
        return StepResult.success(null);
    }

    // 修改簽名
    protected abstract List<CommandStep<?>> steps(C command, CommandContext context);

    protected abstract R buildResponse(CommandContext context);
}
```

✅ **關鍵**：
*   必須在具體 `Service` 上標註 `@Transactional`，保證原子性。
*   審計資訊可通過 `CommandContext` 的 `put/get` 方法在步驟間傳遞。

---

### **🛠️ 使用方式（開發者指南）**

#### **查詢服務開發步驟（以 `CreditQueryService` 為例）**

1.  定義 `QueryRequest` 與 `QueryResponse`
2.  繼承 `QueryTemplate<Req, Resp>`
3.  注入所需 `QueryStep`
4.  實作 `steps(request, context)`, `buildResponse` (可省略 `validate` 如果不需要)
5.  加上 `@QueryFlow` 和 `@Service`

```java
@QueryFlow(code = "CREDIT_ENQUIRY", desc = "信用查詢服務")
@Service
@RequiredArgsConstructor
public class CreditQueryService extends QueryTemplate<QueryRequest, QueryResponse> {
    private final FetchUserFromDbStep fetchUserStep;
    private final FetchCreditFromApiStep fetchCreditStep;

    // validate 方法可省略，使用默認實現

    @Override
    protected List<QueryStep<?>> steps(QueryRequest request, QueryContext context) {
        List<QueryStep<?>> list = new ArrayList<>();
        list.add(fetchUserStep);

        // ✅ 直接在 steps 方法中使用 if-else，清晰直觀！
        if (request.isIncludeCredit()) {
            list.add(fetchCreditStep);
        }

        return list;
    }

    @Override
    protected QueryResponse buildResponse(QueryContext context) {
        User user = context.get("user");
        Credit credit = context.get("credit"); // 可能為 null，如果沒執行
        return new QueryResponse(user, credit);
    }
}
```

#### **寫入服務開發步驟（以 `CreateUserService` 為例）**

1.  定義 `CreateUserCommand`
2.  繼承 `CommandTemplate<Cmd, Resp>`
3.  注入所需 `CommandStep`
4.  實作 `steps(command, context)`, `buildResponse` (可省略 `validate` 如果不需要)
5.  加上 `@CommandFlow`, `@Service`, `@Transactional`

```java
@CommandFlow(code = "CREATE_USER", desc = "創建用戶服務")
@Service
@Transactional
@RequiredArgsConstructor
public class CreateUserService extends CommandTemplate<CreateUserCommand, CreateResponse> {
    private final ValidateUserCreditStep validateStep;
    private final SaveUserToDbStep saveStep;
    private final SendWelcomeEmailStep sendEmailStep;
    private final SendWelcomeSmsStep sendSmsStep;

    // validate 方法可省略，使用默認實現

    @Override
    protected List<CommandStep<?>> steps(CreateUserCommand command, CommandContext context) {
        List<CommandStep<?>> list = new ArrayList<>();
        list.add(validateStep);
        list.add(saveStep);

        // ✅ 直接在 steps 方法中使用 if-else，清晰直觀！
        if ("EMAIL".equals(command.getNotifyType())) {
            list.add(sendEmailStep);
        } else if ("SMS".equals(command.getNotifyType())) {
            list.add(sendSmsStep);
        }

        return list;
    }

    @Override
    protected CreateResponse buildResponse(CommandContext context) {
        User savedUser = context.get("savedUser");
        return new CreateResponse(savedUser.getId());
    }
}
```

#### **步驟 (Step) 開發指南**

步驟是無狀態的、函數式的組件，只依賴於上下文。

```java
@Component // 或使用 @StepComponent (如果您選擇實現它)
public class FetchUserFromDbStep implements QueryStep<User> {
    @Autowired
    private UserRepository userRepo;

    @Override
    public StepResult<User> execute(QueryContext context) {
        QueryRequest request = context.get("request");
        try {
            User user = userRepo.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            context.put("user", user); // 將結果放入上下文，供後續步驟或 buildResponse 使用
            return StepResult.success(user);
        } catch (Exception e) {
            return StepResult.failure("獲取用戶失敗", "USER_FETCH_FAIL", ErrorType.SYSTEM);
        }
    }
}
```

```java
@Component
public class SaveUserToDbStep implements CommandStep<User> {
    @Autowired
    private UserRepository userRepo;

    @Override
    public StepResult<User> execute(CommandContext context) {
        CreateUserCommand command = context.get("command");
        try {
            User user = new User();
            user.setName(command.getName());
            user.setEmail(command.getEmail());
            User savedUser = userRepo.save(user);
            context.put("savedUser", savedUser); // 放入上下文
            return StepResult.success(savedUser);
        } catch (Exception e) {
            return StepResult.failure("保存用戶失敗", "USER_SAVE_FAIL", ErrorType.SYSTEM);
        }
    }
}
```

---

### **🔎 文字流程圖（開發者快速視覺化）**

#### **Query 流程（唯讀）**

```
Client
  |
  v
Controller (HTTP -> DTO)
  |
  v
QueryService (繼承 QueryTemplate)
  |
  |-- validate(request) -> StepResult (可選，有默認實現)
  |
  |-- QueryContext ctx = { request }
  |
  |-- steps(request, ctx) -> 返回步驟列表 (可包含 if-else 條件)
  |
  |-- for step in steps():
  |      result = step.execute(ctx)
  |      if !result.success -> throw BusinessException
  |
  |-- response = buildResponse(ctx)
  |
  v
Controller (DTO -> HTTP 200)
  |
  v
Client
```

#### **Command 流程（含副作用）**

```
Client
  |
  v
Controller (HTTP -> Command)
  |
  v
CommandService (繼承 CommandTemplate)
  |
  |-- validate(command) -> StepResult (可選，有默認實現)
  |
  |-- CommandContext ctx = { command }
  |
  |-- steps(command, ctx) -> 返回步驟列表 (可包含 if-else 條件)
  |
  |-- for step in steps():
  |      result = step.execute(ctx)
  |      if !result.success -> throw BusinessException
  |      (步驟可寫 DB、呼叫外部 API、發布事件)
  |
  |-- response = buildResponse(ctx)
  |
  v
Controller (DTO -> HTTP 201/200)
  |
  v
Client
```

---

### **✅ 控制器層（簡潔一致）**

```java
@RestController
@RequiredArgsConstructor
public class QueryController {
    private final CreditQueryService enquiryService;

    @PostMapping("/api/credit-enquiry")
    public ResponseEntity<QueryResponse> enquiry(@RequestBody QueryRequest req) {
        return ResponseEntity.ok(enquiryService.execute(req));
    }
}

@RestController
@RequiredArgsConstructor
public class CreateUserController {
    private final CreateUserService createUserService;

    @PostMapping("/api/user")
    public ResponseEntity<CreateResponse> create(@RequestBody CreateUserCommand cmd) {
        return ResponseEntity.ok(createUserService.execute(cmd));
    }
}
```

✅ **優點**：控制器極簡，僅做協議轉換，業務邏輯完全下沉。

---

### **✅ 全局異常處理（共用）**

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handle(BusinessException e) {
        ErrorResponse body = new ErrorResponse(e.getErrorCode(), e.getMessage());
        HttpStatus status = switch (e.getErrorType()) {
            case VALIDATION -> HttpStatus.BAD_REQUEST;
            case BUSINESS -> HttpStatus.CONFLICT;
            case SYSTEM -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
        return ResponseEntity.status(status).body(body);
    }
}
```

---

### **✅ ArchUnit 架構稽核（強制合規）**

```java
@Test
void all_enquiry_services_must_be_annotated() {
    classes().that().areAssignableTo(QueryTemplate.class)
             .should().beAnnotatedWith(QueryFlow.class)
             .check(imported);
}

@Test
void all_command_services_must_be_transactional() {
    classes().that().areAnnotatedWith(CommandFlow.class)
             .should().beAnnotatedWith(Transactional.class)
             .check(imported);
}

// 強制禁止 Command 服務相互調用
@Test
void command_services_should_not_call_other_command_services() {
    ArchRule rule = noClasses()
            .that().areAnnotatedWith(CommandFlow.class)
            .should().callClassesThat().areAnnotatedWith(CommandFlow.class)
            .because("Command 服務間相互調用會導致事務邊界混亂，必須禁止。" +
                    "請將復用邏輯提取為 CommandStep。");

    rule.check(imported);
}
```

✅ **價值**：自動化保障架構一致性，防止團隊成員「自由發揮」導致設計腐蝕。

---

### **📊 設計對比總結**

| 特性 | Query (R) | Command (CUD) |
| :--- | :--- | :--- |
| 模板類 | `QueryTemplate<R, S>` | `CommandTemplate<C, R>` |
| 上下文 | `QueryContext` | `CommandContext` |
| 步驟接口 | `QueryStep<T>` | `CommandStep<T>` |
| 服務註解 | `@QueryFlow` | `@CommandFlow` |
| 事務控制 | ❌ 無需事務 | ✅ 必須 `@Transactional` |
| 審計資訊 | 不強制 | ✅ 可通過 `CommandContext` 傳遞 |
| 典型步驟 | 查 DB、調 API | 查詢、寫庫、發事件、權限檢查 |
| 驗證方法 | ✅ 有默認空實現 | ✅ 有默認空實現 |
| 條件編排 | ✅ 在 `steps(request, ctx)` 中用 `if-else` | ✅ 在 `steps(command, ctx)` 中用 `if-else` |

---

### **🏁 結語**

本架構成功實現了 「讓正確的事變得最容易做」 的設計哲學：

*   透過 **模板方法模式**，強制流程統一，杜絕遺漏。
*   透過 **函數式步驟 + 上下文**，實現高內聚、低耦合。
*   透過 **查改分離**，釐清職責，提升系統清晰度。
*   透過 **自定義註解 + ArchUnit**，實現架構可稽核、可治理。
*   透過 **返回純業務物件**，確保服務可被其他模組直接復用，不受 HTTP 層束縛。
*   透過 **`validate` 默認實現**，簡化簡單場景的開發。
*   透過 **`steps(request, context)` + `if-else`**，讓條件化流程編排直觀易懂。
*   透過 **公共 `ErrorType` 枚舉**，確保錯誤分類的一致性和可維護性。

此框架不僅解決當前問題，更為未來的 **流程編排、監控告警、自動化測試、可視化運維** 打下堅實基礎。

✅ **建議作為團隊標準開發規範全面推廣使用。**

--- 

這份文檔現在是真正完整的版本，包含了從核心元件到具體使用的所有細節。您可以將其作為項目的官方規範文檔。