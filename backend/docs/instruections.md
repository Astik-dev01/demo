# Инструкции для Claude Code - Backend Module

## 🌐 Языковые требования
**ВАЖНО**: Все ответы и комментарии должны быть на русском языке.

## 🔒 ЗАПРЕЩЕННЫЕ ИЗМЕНЕНИЯ

**КАТЕГОРИЧЕСКИ ЗАПРЕЩЕНО** изменять следующие базовые классы и методы из модуля `shared`:

### Базовые DTO классы (НЕ ТРОГАТЬ!)
- `BasePageRequest` - базовый класс для всех filter DTO с пагинацией
- `BasePageResponse<T>` - базовый класс для всех ответов с пагинацией  
- `BaseResponse<T>` - базовый wrapper для всех API ответов

### Базовые контроллеры и методы (НЕ ТРОГАТЬ!)
- `BaseController` - базовый класс для всех контроллеров
- `BaseController.getPage()` - конвертация 1-based в 0-based пагинацию
- `BaseController.createSuccessResponse()` - стандартизированные успешные ответы
- `BaseController.createErrorResponse()` - стандартизированные ошибки

### Общие утилиты (НЕ ТРОГАТЬ!)
- `ValidationUtils` - общие методы валидации
- `MessageUtils` - локализация сообщений
- Все exception классы в `shared.exception`

**ПРИЧИНА**: Эти классы используются во всех модулях системы. Любое изменение нарушит совместимость и консистентность архитектуры.

**ЧТО ДЕЛАТЬ ВМЕСТО ИЗМЕНЕНИЙ**:
1. **Наследовать** от базовых классов
2. **Расширять** функционал через новые методы
3. **Использовать** существующие паттерны as-is

## 📝 Стандарты написания кода

### Java Code Style
- Использовать Java 21 features
- Следовать Spring Boot 3.3.5 best practices
- Именование классов: PascalCase
- Именование методов и переменных: camelCase
- Константы: UPPER_SNAKE_CASE
- Пакеты: lowercase

### ⚠️ Imports - СТРОГИЕ ПРАВИЛА

**ГЛАВНОЕ ПРАВИЛО**: В блоке import можно использовать как wildcard (`import package.*`), так и конкретные импорты (`import package.SpecificClass`), но в коде ОБЯЗАТЕЛЬНО использовать только короткие имена без полных путей.

- **✅ ОБА ВАРИАНТА импортов ДОПУСТИМЫ** в секции import:
  ```java
  // Вариант 1: wildcard imports (удобно для множества классов)
  import org.springframework.web.bind.annotation.*;
  import jakarta.persistence.*;
  import jakarta.validation.constraints.*;
  import lombok.*;
  import java.util.*;
  import java.time.*;
  import static java.util.Collections.*;
  
  // Вариант 2: конкретные импорты (точный контроль)
  import org.springframework.web.bind.annotation.RequestBody;
  import org.springframework.web.bind.annotation.PostMapping;
  import jakarta.validation.constraints.NotNull;
  import java.util.List;
  import java.util.ArrayList;
  ```

- **❌ КАТЕГОРИЧЕСКИ ЗАПРЕЩЕНО** использовать полные пути в коде:
  ```java
  // ❌ НЕПРАВИЛЬНО - полные пути в коде ЗАПРЕЩЕНЫ
  @org.springframework.web.bind.annotation.RequestBody
  @jakarta.validation.constraints.NotNull
  new java.util.ArrayList<>();
  java.time.LocalDateTime.now();
  java.util.List<String> list;
  org.springframework.http.ResponseEntity<String> response;
  
  // ✅ ПРАВИЛЬНО - короткие имена после wildcard import
  @RequestBody
  @NotNull
  new ArrayList<>(); 
  LocalDateTime.now();
  List<String> list;
  ResponseEntity<String> response;
  ```

**ВАЖНО**: Любой класс или метод, используемый в коде, ДОЛЖЕН быть импортирован (любым способом - wildcard или конкретно). Главное - чтобы в самом коде не было указаний полных путей к классам или методам.

### 🚫 ДЕТАЛЬНЫЕ ПРИМЕРЫ - Где НЕ использовать полные пути

#### 1. Аннотации (САМАЯ ЧАСТАЯ ОШИБКА):
```java
// ❌ НЕПРАВИЛЬНО
@org.springframework.web.bind.annotation.RequestBody
@org.hibernate.annotations.LazyCollection
@io.swagger.v3.oas.annotations.parameters.RequestBody
@jakarta.validation.constraints.NotNull

// ✅ ПРАВИЛЬНО (после любого импорта - wildcard или конкретного)
@RequestBody
@LazyCollection  
@RequestBody  // для Swagger (разные @RequestBody после соответствующих импортов)
@NotNull
```

#### 2. Создание объектов через new:
```java
// ❌ НЕПРАВИЛЬНО
new org.springframework.core.io.ByteArrayResource(fileData);
new java.io.ByteArrayInputStream(fileData);
new java.util.Random();
new java.util.ArrayList<>();
new com.itextpdf.layout.element.Cell();

// ✅ ПРАВИЛЬНО (после любого импорта - wildcard или конкретного)
new ByteArrayResource(fileData);
new ByteArrayInputStream(fileData);
new Random();
new ArrayList<>();
new Cell();
```

#### 3. Обращение к статическим методам и константам:
```java
// ❌ НЕПРАВИЛЬНО
org.hibernate.annotations.LazyCollectionOption.EXTRA
org.hibernate.annotations.CacheConcurrencyStrategy.NONE
java.time.LocalDateTime.now()
java.util.Collections.emptyList()

// ✅ ПРАВИЛЬНО (после любого импорта - wildcard или конкретного)
LazyCollectionOption.EXTRA
CacheConcurrencyStrategy.NONE
LocalDateTime.now()
Collections.emptyList()
```

#### 4. Объявление переменных и типы в сигнатурах методов:
```java
// ❌ НЕПРАВИЛЬНО
java.util.List<String> list = new java.util.ArrayList<>();
public java.time.LocalDateTime getCreatedAt() { ... }
private java.util.Optional<User> findUser() { ... }

// ✅ ПРАВИЛЬНО (после любого импорта - wildcard или конкретного)
List<String> list = new ArrayList<>();
public LocalDateTime getCreatedAt() { ... }
private Optional<User> findUser() { ... }
```

#### 5. Приведение типов (type casting):
```java
// ❌ НЕПРАВИЛЬНО
(java.util.List<String>) someObject;
(org.springframework.http.ResponseEntity<?>) response;

// ✅ ПРАВИЛЬНО (после любого импорта - wildcard или конкретного)
(List<String>) someObject;
(ResponseEntity<?>) response;
```

### Структура пакетов
```
kg.bishkek.fuel.[module]
├── controller/     # REST контроллеры
├── service/        # Бизнес-логика
├── db/
│   ├── entity/     # JPA сущности
│   ├── repository/ # Spring Data репозитории
│   └── enums/      # Перечисления
├── dto/            # Data Transfer Objects
│   └── hb/{entity}/ # Структура DTO для справочников
│       ├── filter/     # Фильтры для поиска
│       ├── request/    # Запросы на создание/обновление
│       └── response/   # Ответы (включая Page{Entity}Response)
├── mapper/         # MapStruct мапперы
└── config/         # Конфигурации
```

## 📚 Контекст "hb" для справочников (Handbook/Reference Data)

### Когда использовать префикс "hb"

**ВАЖНОЕ ПРАВИЛО**: Префикс "hb" (handbook) добавляется **ТОЛЬКО** для сущностей, которые являются справочниками (reference data).

#### ✅ Когда ИСПОЛЬЗОВАТЬ "hb":
Справочники - это сущности, которые содержат стандартизированные данные для выбора в других сущностях:

```java
// ✅ СПРАВОЧНИКИ - используем префикс "hb"
HBBuyer          // Справочник покупателей
HBFuelType       // Справочник типов топлива  
HBRegion         // Справочник регионов
HBProvince       // Справочник областей
HBSupplier       // Справочник поставщиков
HBTank           // Справочник резервуаров
HBFuelCategory   // Справочник категорий топлива
HBOilDepot       // Справочник нефтебаз
```

#### ❌ Когда НЕ ИСПОЛЬЗОВАТЬ "hb":
Обычные бизнес-сущности, которые представляют основные операции и процессы:

```java
// ❌ ОБЫЧНЫЕ СУЩНОСТИ - БЕЗ префикса "hb"  
Order            // Заказы - основная бизнес-сущность
Invoice          // Накладные - документы  
FuelReception    // Приемка топлива - операция
Inventory        // Инвентарь - состояние
Transaction      // Транзакции - операции
Report           // Отчеты - аналитика
User             // Пользователи - система безопасности
Role             // Роли - система безопасности
```

### Применение правила к именованию

#### Файлы и классы:
```java
// ✅ Справочники
HBBuyer.java                    // Entity
HBBuyerRepository.java          // Repository  
HBBuyerService.java            // Service interface
HBBuyerServiceImpl.java        // Service implementation
HBBuyerController.java         // Controller
HBBuyerRequest.java           // Request DTO
HBBuyerResponse.java          // Response DTO
HBBuyerFilter.java            // Filter DTO
HBBuyerMapper.java            // Mapper

// ❌ Обычные сущности  
Order.java                     // Entity (без hb)
OrderRepository.java           // Repository (без hb)
OrderService.java             // Service (без hb)
OrderController.java          // Controller (без hb)
```

#### Таблицы в БД:
```sql
-- ✅ Справочники - с префиксом hb_
hb_buyers
hb_fuel_types  
hb_regions
hb_suppliers

-- ❌ Обычные сущности - без префикса hb_
orders
invoices  
fuel_receptions
transactions
```

#### URL endpoints:
```java
// ✅ Справочники - в пути присутствует /hb/
@RequestMapping("/oil-depot/hb/buyers")        
@RequestMapping("/oil-depot/hb/fuel-types")    
@RequestMapping("/oil-depot/hb/regions")       

// ❌ Обычные сущности - без /hb/ в пути
@RequestMapping("/oil-depot/orders")           
@RequestMapping("/oil-depot/fuel-receptions")  
@RequestMapping("/oil-depot/reports")          
```

### Критерии определения справочника

**Сущность является справочником, если:**
1. **Используется для выбора** в других формах (dropdown, select)
2. **Стандартизированные данные** (ограниченный набор значений)
3. **Медленно изменяющиеся** данные (редкие обновления)
4. **Переиспользуемые** в разных частях системы
5. **Административные** данные (настройки системы)

**Сущность НЕ является справочником, если:**
1. **Основные бизнес-операции** (заказы, накладные, транзакции)
2. **Часто изменяющиеся** данные (ежедневные операции)
3. **Временные** данные (отчеты, логи, кеш)
4. **Пользовательские** данные (профили, настройки пользователей)
5. **Документооборот** (договоры, акты, справки)

### Важные исключения

Некоторые сущности могут казаться справочниками, но ими не являются:

```java  
// ❌ НЕ справочники, даже если похожи:
User              // Пользователи - это безопасность, не справочник
Company           // Компании - основная бизнес-сущность  
Department        // Отделы - структура организации
Position          // Должности - кадровая структура
Contract          // Договоры - юридические документы
```

## 📊 Префиксы таблиц в базе данных

### Модульные префиксы для таблиц

База данных использует систему префиксов для четкой идентификации принадлежности таблиц к модулям:

#### **`od_`** = Oil Depot (Нефтебаза)
Префикс для всех таблиц модуля управления нефтебазой:

```sql
-- Операционные таблицы
od_fuel_shipments         -- Отгрузки топлива
od_fuel_receptions        -- Приемка топлива
od_fuel_transfers         -- Перемещения топлива
od_fuel_actual_balances   -- Фактические остатки
od_fuel_reception_distributions -- Распределение приемок

-- Справочники (с дополнительным префиксом hb)
od_hb_buyers              -- Справочник покупателей
od_hb_suppliers           -- Справочник поставщиков
od_hb_fuel_types          -- Справочник типов топлива
od_hb_fuel_categories     -- Справочник категорий топлива
od_hb_oil_depots          -- Справочник нефтебаз
od_hb_tanks               -- Справочник резервуаров

-- Связующие таблицы
od_hb_buyer_fuel_types    -- Связь покупатель-типы топлива
od_hb_supplier_fuel_types -- Связь поставщик-типы топлива
od_hb_supplier_oil_depots -- Связь поставщик-нефтебазы
```

#### **`ss_`** = Support System (Техническая поддержка)
Префикс для всех таблиц модуля технической поддержки:

```sql
-- Операционные таблицы
ss_tickets                -- Заявки на техподдержку
ss_ticket_comments        -- Комментарии к заявкам
ss_ticket_files           -- Файлы к заявкам

-- Справочники (с дополнительным префиксом hb)
ss_hb_equipment           -- Справочник оборудования
ss_hb_equipment_categories -- Категории оборудования
ss_hb_equipment_subcategories -- Подкатегории оборудования
ss_hb_category_icons      -- Иконки для категорий
ss_hb_subcategory_icons   -- Иконки для подкатегорий
```

#### **Без префикса** = Общие таблицы (Shared)
Таблицы, используемые всеми модулями:

```sql
-- Аутентификация и авторизация
users                     -- Пользователи системы
roles                     -- Роли пользователей
user_roles                -- Связь пользователь-роль

-- Организационная структура
companies                 -- Компании
gas_stations              -- АЗС (автозаправочные станции)
contractors               -- Подрядчики

-- Системные таблицы
audit_logs                -- Журнал аудита
notifications             -- Уведомления
file_uploads              -- Загруженные файлы
flyway_schema_history     -- История миграций
```

### Правила именования таблиц

1. **Обязательный префикс модуля**: `{module}_` где module = `od` или `ss`
   ```sql
   od_fuel_shipments  ✅ Правильно
   fuel_shipments     ❌ Неправильно (нет префикса модуля)
   ```

2. **Префикс для справочников**: `{module}_hb_` для reference data
   ```sql
   od_hb_buyers       ✅ Правильно (справочник в модуле od)
   od_buyers          ❌ Неправильно (справочник без hb)
   ```

3. **Связующие таблицы**: `{module}_{entity1}_{entity2}`
   ```sql
   od_hb_buyer_fuel_types  ✅ Правильно
   buyer_fuel_types        ❌ Неправильно (нет префикса)
   ```

4. **Всегда snake_case**: все буквы строчные, слова разделены подчеркиванием
   ```sql
   od_fuel_actual_balances ✅ Правильно
   odFuelActualBalances    ❌ Неправильно (camelCase)
   OD_FUEL_ACTUAL_BALANCES ❌ Неправильно (UPPER_CASE)
   ```

### Соответствие Entity классам

При создании JPA Entity классов соблюдать соответствие:

```java
// Для таблицы od_hb_buyers
@Entity
@Table(name = "od_hb_buyers", schema = "support_system_dev")
public class HBBuyer { ... }  // Класс с префиксом HB

// Для таблицы od_fuel_shipments
@Entity
@Table(name = "od_fuel_shipments", schema = "support_system_dev")
public class FuelShipment { ... }  // Класс без префикса HB

// Для таблицы ss_tickets
@Entity
@Table(name = "ss_tickets", schema = "support_system_dev")
public class Ticket { ... }  // Класс без префиксов
```

### Важные замечания

- **od** расшифровывается как **Oil Depot** (нефтебаза), не путать с другими сокращениями
- **ss** расшифровывается как **Support System** (система техподдержки)
- **hb** остается как **Handbook** (справочник) внутри модулей
- Префиксы модулей **обязательны** для всех новых таблиц
- Общие таблицы (users, roles) **не имеют** префиксов модулей

## 🎯 Context7 принципы
При решении задач следовать структуре:
1. **Контекст** - понимание задачи
2. **Анализ** - исследование существующего кода
3. **Решение** - конкретная реализация
4. **Проверка** - валидация результата
5. **Оптимизация** - улучшения если необходимо
6. **Документация** - обновление документации
7. **Завершение** - финальная проверка

## 📖 Swagger/OpenAPI документация

### Контроллеры

#### ✅ ДОПУСТИМЫЕ варианты imports для контроллера:
```java
// Вариант 1: wildcard imports (удобно при использовании множества классов)
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.parameters.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.security.*;
import io.swagger.v3.oas.annotations.tags.*;
import jakarta.servlet.http.*;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import kg.bishkek.fuel.shared.controller.*;
import kg.bishkek.fuel.shared.dto.common.*;

// Вариант 2: конкретные imports (точный контроль зависимостей)
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import kg.bishkek.fuel.shared.controller.BaseController;
import kg.bishkek.fuel.shared.dto.common.BaseResponse;
```

#### Объявление класса:
```java
@Tag(name = "СПРАВОЧНИК Покупатели", description = "API для управления справочником покупателей")
@RestController
@RequestMapping("${oildepot.api.base-path}/hb/buyers")  // ⚠️ Использовать значение из application.properties!
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class HBBuyerController extends BaseController { // ⚠️ ОБЯЗАТЕЛЬНО наследовать от BaseController!
```

#### ⚠️ ВАЖНО: Контекст пути "/oil-depot" из application.properties

**ОБЯЗАТЕЛЬНОЕ ПРАВИЛО**: Для контроллеров модуля нефтебазы всегда использовать значение `${oildepot.api.base-path}` вместо хардкода "/oil-depot".

**✅ ПРАВИЛЬНО - Использование переменной из properties:**
```java
@RequestMapping("${oildepot.api.base-path}/hb/buyers")          
@RequestMapping("${oildepot.api.base-path}/hb/fuel-types")      
@RequestMapping("${oildepot.api.base-path}/hb/regions")         
@RequestMapping("${oildepot.api.base-path}/orders")             
@RequestMapping("${oildepot.api.base-path}/fuel-receptions")    
```

**❌ НЕПРАВИЛЬНО - Хардкод пути:**
```java
@RequestMapping("/oil-depot/hb/buyers")          // ❌ Не хардкодить!
@RequestMapping("/oil-depot/hb/fuel-types")      // ❌ Не хардкодить!
@RequestMapping("/нефтебаза/hb/regions")         // ❌ Не хардкодить!
```

**Настройка в application.properties:**
```properties
# Базовый путь для API модуля нефтебазы
oildepot.api.base-path=/oil-depot
```

**Преимущества использования переменной:**
1. **Централизованное управление** - изменение в одном месте
2. **Гибкость настройки** - разные пути для разных сред (dev, test, prod)
3. **Легкая интернационализация** - можно изменить на `/neftebaza` или `/fuel-depot`
4. **Консистентность** - исключает опечатки в путях

**Примеры разных настроек для разных сред:**
```properties
# Development
oildepot.api.base-path=/oil-depot

# Production (русская локализация)  
oildepot.api.base-path=/нефтебаза

# International
oildepot.api.base-path=/fuel-depot

# Versioned API
oildepot.api.base-path=/v1/oil-depot
```

#### ⚠️ ВАЖНО: Правильное использование @RequestBody аннотаций

**ПРОБЛЕМА**: Конфликт имен между Swagger и Spring аннотациями `@RequestBody`

**РЕШЕНИЕ**: Использовать оба типа аннотаций для разных целей:

1. **Swagger `@RequestBody`** (`io.swagger.v3.oas.annotations.parameters.RequestBody`)
   - Используется для документации в `@Operation`
   - Требует импорт: `import io.swagger.v3.oas.annotations.parameters.RequestBody;`

2. **Spring `@RequestBody`** (`org.springframework.web.bind.annotation.RequestBody`) 
   - Используется для HTTP request body binding в параметрах методов
   - Требует импорт: импортировать через wildcard `import org.springframework.web.bind.annotation.*;`

**КРИТИЧЕСКИ ВАЖНО**: Если не добавить Spring импорт `@RequestBody`, то по умолчанию будет использоваться Swagger импорт, и HTTP запросы **НЕ БУДУТ РАБОТАТЬ**!

**✅ ПРАВИЛЬНЫЙ пример контроллера:**
```java
// Импорты
import io.swagger.v3.oas.annotations.parameters.RequestBody; // Для Swagger документации
import org.springframework.web.bind.annotation.*;              // Для Spring @RequestBody

@PostMapping("/filter")
@Operation(
    summary = "Фильтрация покупателей",
    requestBody = @RequestBody(  // ← Swagger аннотация для документации
        required = true,
        description = "DTO для фильтрации",
        content = @Content(schema = @Schema(implementation = HBBuyerFilter.class))
    )
)
public ResponseEntity<BaseResponse<PageHBBuyerResponse>> findAllWithFilter(
    @RequestBody(required = false) HBBuyerFilter filter // ← Spring аннотация для HTTP
) {
    return createSuccessResponse(buyerService.findAllWithFilter(filter));
}
```

**❌ НЕПРАВИЛЬНО** - использовать полное имя в коде:
```java
// НИКОГДА не делать так:
public ResponseEntity<BaseResponse<PageHBBuyerResponse>> findAllWithFilter(
    @org.springframework.web.bind.annotation.RequestBody(required = false) HBBuyerFilter filter
) {
```

**СИМПТОМЫ проблемы**: Если Spring `@RequestBody` импорт отсутствует:
- Компиляция проходит успешно 
- Swagger документация работает
- HTTP POST запросы возвращают ошибки валидации или пустой request body
- В параметрах методов используется Swagger аннотация вместо Spring

**ПРОФИЛАКТИКА**: Всегда проверять в контроллерах HB (handbook) модулей наличие обоих импортов:
- `import io.swagger.v3.oas.annotations.parameters.RequestBody;`
- `import org.springframework.web.bind.annotation.*;` (содержит Spring RequestBody)

### Endpoints
```java
@GetMapping("/{id}")
@Operation(
    summary = "Получить покупателя по ID",
    responses = @ApiResponse(content = @Content(schema = @Schema(implementation = HBBuyerResponse.class)))
)
public ResponseEntity<? extends BaseResponse<?>> findById(
    @Parameter(description = "ID покупателя", example = "1") @PathVariable Long id
) {
    // ⚠️ ИСПОЛЬЗОВАТЬ createSuccessResponse() из BaseController!
```

### Endpoints с RequestBody
```java
@PostMapping("/filter")
@Operation(
    summary = "Фильтрация и постраничное получение списка покупателей",
    requestBody = @RequestBody(
        required = true,
        description = "DTO для фильтрации покупателей",
        content = @Content(schema = @Schema(implementation = HBBuyerFilter.class))
    ),
    responses = @ApiResponse(content = @Content(schema = @Schema(implementation = PageHBBuyerResponse.class)))
)
public ResponseEntity<? extends BaseResponse<?>> findAllWithFilter(
    @RequestBody(required = false) HBBuyerFilter filter
) {
```

### DTO документация
```java
@Schema(description = "Ответ с данными покупателя")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HBBuyerResponse {
    
    @Schema(description = "Идентификатор покупателя", example = "1")
    private Long id;
    
    @Schema(description = "Наименование покупателя", example = "ОсОО Нефтегаз Плюс")
    private String name;
    
    @Schema(description = "Условия оплаты", example = "PREPAYMENT", implementation = HBPaymentTerms.class)
    private HBPaymentTerms paymentTerms;
    
    @Schema(description = "Виды топлива покупателя", implementation = HBFuelTypeResponse.class)
    private Set<HBFuelTypeResponse> fuelTypes;
    
    @Schema(description = "Дата и время создания", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "Дата и время последнего обновления", example = "2024-02-20T14:45:00")
    private LocalDateTime updatedAt;
    
    // ❌ НЕ добавлять служебные поля:
    // private Long createdBy;
    // private Long updatedBy;
}
```

### ✅ Пример правильной Response DTO структуры с полными объектами:
```json
{
  "id": 1,
  "name": "ОсОО Нефтегаз Плюс",
  "inn": "02010199612345",
  "email": "buyer@example.com",
  "phone": "+996555987654",
  "paymentTerms": "PREPAYMENT",
  "region": {
    "id": 1,
    "name": "Чуйская область",
    "code": "CHU",
    "province": {
      "id": 1,
      "name": "Кыргызстан",
      "code": "KG"
    }
  },
  "fuelTypes": [
    {
      "id": 1,
      "name": "АИ-92",
      "code": "AI92",
      "category": {
        "id": 1,
        "name": "Бензин"
      }
    },
    {
      "id": 2,
      "name": "АИ-95",
      "code": "AI95",
      "category": {
        "id": 1,
        "name": "Бензин"
      }
    }
  ],
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-02-20T14:45:00"
}
```

**Важно**: Связанные объекты (region, fuelTypes) возвращаются как **полные Response DTO**, а НЕ как отдельные поля (regionId, regionName).

**ВАЖНО для DTO**:
- **Filter DTO**: должен содержать ВСЕ поля для фильтрации из Entity + **ОБЯЗАТЕЛЬНО** наследовать BasePageRequest (**НЕ ИЗМЕНЯТЬ базовый класс!**)
- **Request DTO**: должен содержать ВСЕ поля для создания/обновления (кроме системных: createdAt, updatedAt, createdBy, updatedBy)
- **Response DTO**: должен содержать ВСЕ поля из Entity (кроме служебных: **createdBy, updatedBy**)
- **Page Response DTO**: **ОБЯЗАТЕЛЬНО** наследовать BasePageResponse<T> (**НЕ ИЗМЕНЯТЬ базовый класс!**)

#### 🎯 Поля в Response DTO - ЧЕТКАЯ СТРУКТУРА:

##### ✅ **ОБЯЗАТЕЛЬНЫЕ поля (включать в Response):**
- **`id`** - идентификатор сущности (обязательно для фронтенда)
- **`createdAt`** - дата создания (полезно для UI, сортировки, фильтрации)  
- **`updatedAt`** - дата обновления (полезно для UI, отслеживания изменений)

##### ❌ **СЛУЖЕБНЫЕ поля (исключать из Response):**
- **`createdBy`** - ID создавшего пользователя (внутренняя служебная информация)
- **`updatedBy`** - ID обновившего пользователя (внутренняя служебная информация)
- **`deleted`** - флаг мягкого удаления (техническое поле, клиенту не нужно)

##### 🔗 **СВЯЗАННЫЕ объекты (включать как полные Response DTO):**
- **`region`** → `HBRegionResponse` (полный объект с вложенными данными)
- **`fuelTypes`** → `Set<HBFuelTypeResponse>` (коллекция полных объектов)
- **`category`** → `HBCategoryResponse` (полный объект с province, если есть)

##### 📋 **ПРАВИЛА для связанных объектов:**
- **✅ ПОЛНЫЕ объекты**: `region: { id, name, code, province: { id, name, code } }`
- **❌ НЕ разворачивать**: `regionId, regionName, provinceName` (отдельными полями)
- **✅ ВЛОЖЕННОСТЬ**: Связанные объекты содержат свои связанные объекты

### 🎯 ЕДИНСТВЕННЫЙ ПРАВИЛЬНЫЙ подход для связанных объектов:
- **В Request используем ID** (например: `regionId: 1`, `fuelTypeIds: [1, 2]`)
- **В Response возвращаем ПОЛНЫЕ объекты Response DTO** (например: `region: HBRegionResponse`, `fuelTypes: Set<HBFuelTypeResponse>`)
- **ЗАПРЕЩЕНО разворачивать** в отдельные поля (`regionId`, `regionName`, `provinceName`)
- **Связанные объекты должны быть вложенными** с полной структурой

- **Типы данных**: должны соответствовать типам в Entity

## 🎯 Обязательные endpoints и их порядок

### Порядок методов в контроллере
```java
1. GET /{id}          // Получить по ID
2. POST /filter       // ЕДИНСТВЕННЫЙ метод для поиска, фильтрации и пагинации
3. POST               // Создать новую запись
4. PUT                // Обновить запись
5. DELETE /{id}       // Удалить запись (мягкое удаление)
```

**ВАЖНО**: 
- НЕ создавать отдельные методы GET /all, GET /search, GET /find-by-name и т.д.
- ВСЕ поисковые операции реализуются через POST /filter
- Метод POST /filter универсальный - может работать без фильтров (вернет все записи с пагинацией)

### Порядок методов в сервисе
```java
1. create()           // Создание записи
2. update()           // Обновление записи
3. delete()           // Удаление записи
4. get()              // Получить по ID (возвращает DTO)
5. findById()         // Внутренний метод поиска (возвращает Entity)
6. findAllWithFilter() // ЕДИНСТВЕННЫЙ метод для поиска с фильтрацией
```

**ВАЖНО**:
- НЕ создавать методы findAll(), findByName(), searchBy...() и т.д.
- ВСЯ логика поиска реализуется в findAllWithFilter() через Specifications

## 🏗️ Архитектурные паттерны

### Модульная архитектура
- **main** - точка входа приложения
- **shared** - общие компоненты
- **support-system** - система техподдержки АЗС
- **oil-depot** - управление нефтебазой
- **gas-station** - управление АЗС
- **notification** - уведомления

### Service Layer Pattern
```java
@Service
@Transactional
@RequiredArgsConstructor
public class HBBuyerServiceImpl implements HBBuyerService {
    private final HBBuyerRepository buyerRepository;
    private final HBBuyerMapper buyerMapper;
    private final HBRegionService regionService; // Для связанных объектов
    
    // ⚠️ ВАЖНО: ВСЕГДА использовать BaseController.getPage() для пагинации!
    
    @Override
    public HBBuyerResponse create(HBBuyerRequest request, HttpServletRequest httpRequest) {
        // Проверка уникальности
        checkUniqueness(request);
        
        // Маппинг ВСЕХ полей из request в entity
        HBBuyer entity = buyerMapper.toEntity(request);
        
        // Установка связанных объектов (не забываем!)
        if (request.getRegionId() != null) {
            entity.setRegion(regionService.findById(request.getRegionId()));
        }
        
        // Сохранение (системные поля устанавливаются автоматически через BaseEntity)
        entity = buyerRepository.save(entity);
        
        // Возврат ПОЛНОГО Response со всеми полями
        return buyerMapper.toResponse(entity);
    }
    
    @Override
    public HBBuyerResponse get(Long id) {
        HBBuyer entity = findById(id);
        // Mapper должен заполнить ВСЕ поля, включая связанные объекты
        return buyerMapper.toResponse(entity);
    }
}
```

**ВАЖНО для Service**:
- **ВСЕ поля из Request** должны быть записаны в Entity
- **ВСЕ связанные объекты** должны быть загружены и установлены
- **Response должен быть ПОЛНЫМ** - без null полей (кроме необязательных)
- Для связанных объектов использовать соответствующие сервисы

### Repository Specifications
```java
public class EntitySpecifications {
    public static Specification<Entity> byName(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isEmpty()) {
                return cb.conjunction();
            }
            return cb.like(
                cb.lower(root.get("name")), 
                "%" + name.toLowerCase() + "%"
            );
        };
    }
}
```

## 🗄️ База данных

### Миграции Flyway
- Файлы миграций: `V{version}__{description}.sql`
- Расположение: `src/main/resources/db/migration/`
- Версии: V001, V002, V003...
- Пример: `V007__HB_OilDepot_Schema.sql`

### Порядок создания объектов для ОДНОЙ таблицы
```sql
-- 1. Создание таблицы
CREATE TABLE IF NOT EXISTS hb_provinces (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(10) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    deleted BOOLEAN DEFAULT FALSE
);

-- 2. Создание индексов (сначала уникальные, потом обычные)
CREATE INDEX IF NOT EXISTS idx_hb_provinces_code ON hb_provinces(code);
CREATE INDEX IF NOT EXISTS idx_hb_provinces_name ON hb_provinces(name);
CREATE INDEX IF NOT EXISTS idx_hb_provinces_deleted ON hb_provinces(deleted);

-- 3. Комментарий к таблице
COMMENT ON TABLE hb_provinces IS 'СПРАВОЧНИК: Области Кыргызстана';

-- 4. Комментарии к полям
COMMENT ON COLUMN hb_provinces.id IS 'Уникальный идентификатор';
COMMENT ON COLUMN hb_provinces.name IS 'Наименование области';
COMMENT ON COLUMN hb_provinces.code IS 'Код области';
COMMENT ON COLUMN hb_provinces.created_at IS 'Дата создания записи';
COMMENT ON COLUMN hb_provinces.created_by IS 'ID пользователя создавшего запись';
COMMENT ON COLUMN hb_provinces.updated_at IS 'Дата последнего обновления';
COMMENT ON COLUMN hb_provinces.updated_by IS 'ID пользователя обновившего запись';
COMMENT ON COLUMN hb_provinces.deleted IS 'Флаг мягкого удаления';
```

**ВАЖНО**: 
- Все элементы одной таблицы идут последовательно
- Обязательно использовать IF NOT EXISTS для таблиц и индексов
- Не перемешивать элементы разных таблиц

### Именование в БД
- Таблицы: snake_case, множественное число (users, oil_depots)
- Колонки: snake_case (created_at, user_name)
- Индексы: idx_{table}_{columns} (idx_users_email)
- Foreign keys: fk_{table}_{ref_table} (fk_tickets_user)

### JPA Entity
```java
@Entity
@Table(name = "entity_names")
@Data
@Builder  
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class EntityName extends BaseEntity {
    // БЕЗ JavaDoc комментариев!
    // БЕЗ валидационных аннотаций!
    
    // ✅ ОПТИМИЗИРОВАННЫЕ аннотации - указываем только отличия от умолчания
    
    @Column(name = "name", nullable = false)  // nullable отличается от умолчания
    private String name;
    
    @Column(name = "code", nullable = false, unique = true, length = 20)  // все параметры отличаются
    private String code;
    
    @Column(name = "email")  // length=255, nullable=true по умолчанию - НЕ указываем
    private String email;
    
    @Column(name = "description")  // только name, остальное по умолчанию
    private String description;
    
    @Column(name = "price", precision = 10, scale = 2)  // для BigDecimal указываем precision/scale
    private BigDecimal price;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)  // для enum указываем length != 255
    private EntityStatus status;
    
    // ✅ Связи - указываем только отличающиеся параметры
    @ManyToOne(fetch = FetchType.LAZY)  // LAZY отличается от умолчания EAGER
    @JoinColumn(name = "parent_id")  // nullable=true по умолчанию - НЕ указываем  
    private ParentEntity parent;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)  // обязательная связь
    private CategoryEntity category;
    
    @OneToMany(mappedBy = "entity", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default  // для коллекций
    private List<ChildEntity> children = new ArrayList<>();
}
```

**ВАЖНО для Entity**:
- НЕ добавлять JavaDoc комментарии
- НЕ использовать валидационные аннотации (@NotNull, @Size, @Email и т.д.)
- Валидация ТОЛЬКО в Request DTO классах
- **СООТВЕТСТВИЕ ПОЛЕЙ**: Entity должна содержать ВСЕ поля из миграции БД
- **СООТВЕТСТВИЕ ТИПОВ**: Типы данных в Entity должны точно соответствовать типам в БД:
  - VARCHAR → String
  - BIGINT/BIGSERIAL → Long
  - INTEGER → Integer
  - DECIMAL → BigDecimal
  - TIMESTAMP → LocalDateTime
  - BOOLEAN → Boolean

### ⚡ JPA Аннотации - Правила оптимизации

**ГЛАВНЫЙ ПРИНЦИП**: НЕ указывать параметры аннотаций, которые соответствуют значениям по умолчанию.

#### ❌ ИЗБЫТОЧНЫЕ параметры (НЕ указывать):
```java
// ❌ НЕПРАВИЛЬНО - избыточные параметры
@Column(name = "email", length = 255, nullable = true)
@Column(name = "description", length = 255, nullable = true)
@Column(name = "status", length = 255, nullable = true)

// ❌ НЕПРАВИЛЬНО - дублирование значений по умолчанию
@JoinColumn(name = "user_id", nullable = true)
@ManyToOne(fetch = FetchType.EAGER, optional = true) // EAGER и optional=true по умолчанию
```

#### ✅ ОПТИМИЗИРОВАННЫЕ аннотации (указывать только отличия от умолчания):
```java
// ✅ ПРАВИЛЬНО - только name, length=255 и nullable=true по умолчанию
@Column(name = "email")
@Column(name = "description") 
@Column(name = "status")

// ✅ ПРАВИЛЬНО - указываем только отличающиеся параметры
@Column(name = "name", nullable = false)           // только nullable отличается
@Column(name = "code", length = 10)               // только length отличается  
@Column(name = "inn", nullable = false, length = 14) // оба параметра отличаются

// ✅ ПРАВИЛЬНО - LAZY отличается от умолчания EAGER
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id", nullable = false)   // только nullable отличается
```

#### 🎯 Конкретные правила по аннотациям:

**@Column параметры по умолчанию:**
- `length = 255` для String
- `nullable = true` 
- `unique = false`
- `updatable = true`
- `insertable = true`

**@ManyToOne/@OneToOne параметры по умолчанию:**
- `fetch = FetchType.EAGER`
- `optional = true` (= nullable = true)

**@OneToMany/@ManyToMany параметры по умолчанию:**
- `fetch = FetchType.LAZY`

**@JoinColumn параметры по умолчанию:**
- `nullable = true`
- `unique = false`
- `updatable = true`
- `insertable = true`

#### 📋 Примеры оптимизации из реального кода:

```java
// ❌ БЫЛО (избыточно)
@Column(name = "email", length = 255, nullable = true)
@Column(name = "contact_person", length = 255, nullable = true) 
@ManyToOne(fetch = FetchType.LAZY, optional = true)
@JoinColumn(name = "region_id", nullable = false, updatable = true)

// ✅ СТАЛО (оптимизировано)
@Column(name = "email")
@Column(name = "contact_person")
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "region_id", nullable = false)
```

## 🔄 MapStruct мапперы

### ⚠️ КРИТИЧЕСКИ ВАЖНО: Repository VS Service в мапперах

**ПРАВИЛО**: Мапперы должны использовать **ТОЛЬКО Service**, а НЕ Repository для получения связанных сущностей!

#### ❌ НЕПРАВИЛЬНЫЙ подход - Repository в маппере:
```java
// ❌ АРХИТЕКТУРНОЕ НАРУШЕНИЕ
@Component
@RequiredArgsConstructor
public class SomeMapper {
    
    private final SomeRepository someRepository;  // ❌ Repository в маппере ЗАПРЕЩЕН!
    
    public Entity toEntity(Request request) {
        return Entity.builder()
            .relatedEntity(someRepository.findById(request.getRelatedId())  // ❌ Маппер делает запросы к БД!
                .orElseThrow(() -> new ResourceNotFoundException("Not found")))
            .build();
    }
}
```

**Проблемы с Repository в маппере:**
1. **Нарушение архитектуры** - маппер выполняет бизнес-логику
2. **Нарушение SRP** - маппер отвечает за преобразование, а не за получение данных  
3. **Проблемы с транзакциями** - маппер может выполняться вне транзакционного контекста
4. **Сложность тестирования** - нужно мокать Repository в тестах маппера
5. **Циркулярные зависимости** - может привести к проблемам с Spring Context

#### ✅ ПРАВИЛЬНЫЙ подход - Ignore в маппере + Service в слое сервиса:

**Шаг 1: Маппер игнорирует связанные поля**
```java
@Mapper(componentModel = "spring")
public interface HBBuyerMapper {
    
    // Request → Entity (игнорируем системные поля и связанные объекты)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "region", ignore = true)    // ✅ Игнорируем связанные объекты
    @Mapping(target = "fuelTypes", ignore = true) // ✅ Игнорируем связанные объекты
    HBBuyer toEntity(HBBuyerRequest request);
    
    // Entity → Response (маппим ВСЕ поля, исключая служебные createdBy/updatedBy)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    HBBuyerResponse toResponse(HBBuyer entity);
    
    // Page → PageResponse
    default PageHBBuyerResponse toPageResponse(Page<HBBuyer> page) {
        PageHBBuyerResponse response = new PageHBBuyerResponse();
        response.setContent(page.getContent().stream()
            .map(this::toResponse)
            .collect(Collectors.toList()));
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setNumber(page.getNumber() + 1); // 1-based
        return response;
    }
    
    // Обновление Entity из Request (также игнорируем связанные поля)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "region", ignore = true)    // ✅ Игнорируем связанные объекты
    @Mapping(target = "fuelTypes", ignore = true) // ✅ Игнорируем связанные объекты
    void updateEntityFromRequest(@MappingTarget HBBuyer entity, HBBuyerRequest request);
}
```

**Шаг 2: Service устанавливает связанные объекты**
```java
@Service
@RequiredArgsConstructor
public class HBBuyerServiceImpl implements HBBuyerService {
    
    private final HBBuyerRepository buyerRepository;
    private final HBBuyerMapper buyerMapper;
    private final HBRegionService regionService;      // ✅ Service для связанных объектов
    private final HBFuelTypeService fuelTypeService;  // ✅ Service для связанных объектов
    
    @Override
    @Transactional
    public HBBuyerResponse create(HBBuyerRequest request, HttpServletRequest httpRequest) {
        
        // Маппер создает базовую сущность БЕЗ связанных объектов
        HBBuyer entity = buyerMapper.toEntity(request);
        
        // ✅ Service устанавливает связанные объекты через другие Service
        if (request.getRegionId() != null) {
            entity.setRegion(regionService.findById(request.getRegionId()));
        }
        
        if (request.getFuelTypeIds() != null && !request.getFuelTypeIds().isEmpty()) {
            Set<HBFuelType> fuelTypes = new HashSet<>();
            for (Long fuelTypeId : request.getFuelTypeIds()) {
                HBFuelType fuelType = fuelTypeService.findById(fuelTypeId);
                fuelTypes.add(fuelType);
            }
            entity.setFuelTypes(fuelTypes);
        }
        
        entity = buyerRepository.save(entity);
        return buyerMapper.toResponse(entity);
    }
}
```

### 📋 Ключевые принципы для мапперов:

#### **Ответственность мапперов:**
- **ТОЛЬКО преобразование типов** (DTO ↔ Entity)
- **НЕ получение данных** из БД 
- **НЕ бизнес-логика**
- **НЕ валидация данных**

#### **toEntity() метод:**
- Маппить ВСЕ простые поля из Request DTO
- **ИГНОРИРОВАТЬ** все связанные объекты (`@Mapping(target = "relatedField", ignore = true)`)
- **ИГНОРИРОВАТЬ** все системные поля (`id`, `createdAt`, `updatedAt`, `createdBy`, `updatedBy`, `deleted`)

#### **toResponse() метод:**
- Маппить ВСЕ поля включая связанные объекты как **полные Response DTO**
- **ИСКЛЮЧАТЬ** только служебные поля (`createdBy`, `updatedBy`)
- Связанные объекты должны быть уже загружены в Entity

#### **updateEntityFromRequest() метод:**
- **ИГНОРИРОВАТЬ** связанные объекты - они обновляются в Service
- **ИГНОРИРОВАТЬ** системные поля
- Использовать `NullValuePropertyMappingStrategy.IGNORE`

### 🔧 Рекомендации по исправлению существующего кода

#### Если у вас есть маппер с Repository (как HBFuelReceptionDistributionMapper):

**Шаг 1: Переписать маппер на MapStruct interface**
```java
// Заменить @Component класс на @Mapper interface
@Mapper(componentModel = "spring") 
public interface HBFuelReceptionDistributionMapper {
    
    @Mapping(target = "tank", ignore = true)  // ✅ Игнорируем связанное поле
    HBFuelReceptionDistribution toEntity(HBFuelReceptionDistributionRequest request);
    
    HBFuelReceptionDistributionResponse toResponse(HBFuelReceptionDistribution entity);
    
    @Mapping(target = "tank", ignore = true)  // ✅ Игнорируем связанное поле  
    void updateEntityFromRequest(@MappingTarget HBFuelReceptionDistribution entity, 
                                HBFuelReceptionDistributionRequest request);
}
```

**Шаг 2: Обновить Service для установки связанных объектов**
```java
@Service
@RequiredArgsConstructor
public class HBFuelReceptionServiceImpl implements HBFuelReceptionService {
    
    private final HBTankService tankService;  // ✅ Service для связанных объектов
    
    @Override
    @Transactional
    public HBFuelReceptionResponse create(HBFuelReceptionRequest request, HttpServletRequest httpRequest) {
        
        HBFuelReception entity = fuelReceptionMapper.toEntity(request);
        
        // Устанавливаем связанные объекты для каждого distribution
        if (request.getDistributions() != null) {
            for (HBFuelReceptionDistribution distribution : entity.getDistributions()) {
                // ✅ Service устанавливает связанные объекты
                if (distribution.getTankId() != null) {  // Нужно добавить tankId в Request
                    distribution.setTank(tankService.findById(distribution.getTankId()));
                }
            }
        }
        
        entity = fuelReceptionRepository.save(entity);
        return fuelReceptionMapper.toResponse(entity);
    }
}
```

### 🚫 ЗАПРЕТЫ для мапперов:

1. **❌ НЕ ИСПОЛЬЗОВАТЬ Repository** в мапперах
2. **❌ НЕ ВЫПОЛНЯТЬ запросы к БД** в мапперах  
3. **❌ НЕ ДОБАВЛЯТЬ бизнес-логику** в мапперы
4. **❌ НЕ ДЕЛАТЬ валидацию** в мапперах
5. **❌ НЕ УСТАНАВЛИВАТЬ связанные объекты** в мапперах

### ✅ ЧТО ДЕЛАТЬ вместо этого:

1. **✅ ИСПОЛЬЗОВАТЬ Service** для получения связанных объектов
2. **✅ ИГНОРИРОВАТЬ связанные поля** в мапперах через `@Mapping(ignore = true)`
3. **✅ УСТАНАВЛИВАТЬ связанные объекты** в Service слое
4. **✅ ИСПОЛЬЗОВАТЬ MapStruct interface** вместо @Component класса
5. **✅ ДЕРЖАТЬ мапперы простыми** - только преобразование типов

### 🔧 **МИГРАЦИЯ @Component → @Mapper interface**

#### Если у вас есть существующий @Component маппер:

**ШАГ 1: Заменить @Component класс на @Mapper interface**
```java
// ❌ СТАРЫЙ подход (удалить)
@Component
@RequiredArgsConstructor
public class HBSomeEntityMapper {
    private final HBRelatedRepository relatedRepository; // ❌ Repository в маппере!
    
    public HBSomeEntity toEntity(HBSomeEntityRequest request) {
        // ❌ Ручная реализация с запросами к БД
        return HBSomeEntity.builder()
            .name(request.getName())
            .relatedEntity(relatedRepository.findById(request.getRelatedId())
                .orElseThrow(() -> new ResourceNotFoundException("Not found")))
            .build();
    }
}

// ✅ НОВЫЙ подход (создать)
@Mapper(componentModel = "spring")
public interface HBSomeEntityMapper {
    @Mapping(target = "relatedEntity", ignore = true) // ✅ Игнорируем, устанавливается в Service
    HBSomeEntity toEntity(HBSomeEntityRequest request);
    
    HBSomeEntityResponse toResponse(HBSomeEntity entity);
}
```

**ШАГ 2: Перенести логику установки связанных объектов в Service**
```java
@Service  
@RequiredArgsConstructor
public class HBSomeEntityServiceImpl implements HBSomeEntityService {
    private final HBRelatedService relatedService; // ✅ Service вместо Repository
    private final HBSomeEntityMapper mapper;
    
    @Override
    public HBSomeEntityResponse create(HBSomeEntityRequest request, HttpServletRequest httpRequest) {
        HBSomeEntity entity = mapper.toEntity(request); // Маппер создает базовую сущность
        
        // ✅ Service устанавливает связанные объекты
        if (request.getRelatedId() != null) {
            entity.setRelatedEntity(relatedService.findById(request.getRelatedId()));
        }
        
        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }
}
```

## ⚠️ Обработка ошибок
```java
// Использовать custom exceptions из shared модуля
throw new ResourceNotFoundException("Ресурс не найден: " + id);
throw new BadRequestException("Некорректные данные: " + field);
throw new ConflictException("Ресурс уже существует: " + name);
```

## 🔍 Поиск и фильтрация

**ЕДИНСТВЕННЫЙ правильный способ** для поиска и фильтрации:

```java
@PostMapping("/filter")
@Operation(
    summary = "Фильтрация и постраничное получение списка",
    requestBody = @RequestBody(
        description = "DTO для фильтрации",
        content = @Content(schema = @Schema(implementation = EntityFilter.class))
    ),
    responses = @ApiResponse(content = @Content(schema = @Schema(implementation = PageEntityResponse.class)))
)
public ResponseEntity<BaseResponse<PageEntityResponse>> findAllWithFilter(
    @RequestBody(required = false) EntityFilter filter
) {
    if (filter == null) {
        filter = new EntityFilter();
    }
    return createSuccessResponse(service.findAllWithFilter(filter));
}
```

**Важно**: 
- ТОЛЬКО POST /filter для всех поисков
- НИКАКИХ GET /all, GET /search, GET /find-by-name
- Всегда использовать BaseController.createSuccessResponse()

## 💡 Важные принципы
1. **DRY** - не дублировать код, использовать shared модуль
2. **KISS** - простые и понятные решения
3. **SOLID** - следовать принципам ООП
4. **RESTful** - соблюдать REST conventions
5. **Безопасность** - валидация входных данных, проверка прав доступа

## 🚫 Что НЕ делать

### ⛔ КАТЕГОРИЧЕСКИЙ ЗАПРЕТ - Базовые классы
- **НЕ ИЗМЕНЯТЬ** `BasePageRequest`, `BasePageResponse`, `BaseResponse` 
- **НЕ ИЗМЕНЯТЬ** `BaseController` и его методы (`getPage()`, `createSuccessResponse()`, `createErrorResponse()`)
- **НЕ ИЗМЕНЯТЬ** общие утилиты из shared модуля (`ValidationUtils`, `MessageUtils`)
- **НЕ СОЗДАВАТЬ** дублирующие базовые классы

### 🚫 Архитектурные запреты  
- НЕ использовать Hibernate auto-ddl (только validate)
- НЕ хардкодить значения (использовать application.properties)
- НЕ игнорировать null checks
- НЕ смешивать бизнес-логику с контроллерами
- НЕ создавать God Classes
- НЕ использовать публичные поля в классах
- НЕ создавать лишние endpoints для поиска (GET /all, GET /search, GET /by-name)

### 🚫 Код-стайл запреты

#### **КРИТИЧЕСКИ ВАЖНО - Правила импортов:**
- **✅ ДОПУСТИМЫ ОБА ВАРИАНТА** imports в блоке import:
  ```java
  // Wildcard imports
  import org.springframework.web.bind.annotation.*;
  import jakarta.validation.constraints.*;  
  import java.util.*;
  
  // Конкретные imports  
  import org.springframework.web.bind.annotation.RequestBody;
  import jakarta.validation.constraints.NotNull;
  import java.util.List;
  ```
- **❌ КАТЕГОРИЧЕСКИ НЕ ИСПОЛЬЗОВАТЬ полные пути** в коде (аннотации, new, статические вызовы, объявления переменных):
  - `@org.springframework.web.bind.annotation.RequestBody` → `@RequestBody`  
  - `@jakarta.validation.constraints.NotNull` → `@NotNull`
  - `new java.util.ArrayList<>()` → `new ArrayList<>()`
  - `java.time.LocalDateTime.now()` → `LocalDateTime.now()`
  - `java.util.List<String> list` → `List<String> list`
  - `org.springframework.http.HttpStatus.OK` → `HttpStatus.OK` (или `OK` через static import)

#### **Мапперы - КАТЕГОРИЧЕСКИЕ ЗАПРЕТЫ:**
- **❌ НЕ ИСПОЛЬЗОВАТЬ @Component** для мапперов
- **❌ НЕ СОЗДАВАТЬ ручные мапперы** как обычные классы
- **❌ НЕ ИСПОЛЬЗОВАТЬ Repository** в мапперах (только Service)
- **✅ ТОЛЬКО MapStruct @Mapper interface** разрешены

```java
// ❌ КАТЕГОРИЧЕСКИ ЗАПРЕЩЕНО
@Component
@RequiredArgsConstructor  
public class SomeMapper {
    private final SomeRepository repository; // ❌ Repository в маппере!
    
    public Entity toEntity(Request request) {
        // ❌ Ручная реализация маппинга
        return Entity.builder()
            .field(request.getField())
            .relatedEntity(repository.findById(request.getRelatedId()).orElseThrow())
            .build();
    }
}

// ✅ ПРАВИЛЬНО - MapStruct interface
@Mapper(componentModel = "spring")
public interface SomeMapper {
    @Mapping(target = "relatedEntity", ignore = true) // ✅ Игнорируем, устанавливается в Service
    Entity toEntity(Request request);
}
```

#### **Другие запреты:**
- **НЕ ПУТАТЬ аннотации** - различать Swagger `@RequestBody` от Spring `@RequestBody`
- НЕ добавлять JavaDoc комментарии в Entity классы
- НЕ добавлять валидацию в Entity (только в Request DTO)  
- **НЕ УКАЗЫВАТЬ избыточные JPA параметры** - не добавлять `length = 255`, `nullable = true` в @Column
- **НЕ УКАЗЫВАТЬ параметры по умолчанию** в JPA аннотациях (@ManyToOne, @JoinColumn и т.д.)
- **НЕ ВКЛЮЧАТЬ служебные поля** `createdBy`, `updatedBy` в Response DTO
- НЕ нарушать принципы наследования от базовых классов
- НЕ дублировать логику пагинации (использовать `BaseController.getPage()`)

## 📋 Чеклист при создании нового функционала
- [ ] Создана миграция БД (с IF NOT EXISTS, индексами и комментариями)
- [ ] Создана JPA Entity (все поля из БД, без комментариев и валидации)
- [ ] Создан Repository с Specifications
- [ ] Созданы DTO:
  - [ ] Filter (все поля для фильтрации + extends BasePageRequest)
  - [ ] Request (все поля кроме системных, с валидацией)
  - [ ] Response (ВСЕ поля из Entity, связанные объекты как Response DTO)
- [ ] Создан Mapper (маппинг ВСЕХ полей)
- [ ] Создан Service interface и implementation:
  - [ ] Все поля из Request записываются в Entity
  - [ ] Все связанные объекты загружаются и устанавливаются
  - [ ] Response возвращается полностью заполненным
- [ ] Создан Controller (только 5 обязательных endpoints)
- [ ] Добавлена Swagger документация (с примерами и implementation)
- [ ] Добавлена обработка ошибок
- [ ] Реализована пагинация через POST /filter

## 🎨 Примеры хороших практик

### Валидация (ТОЛЬКО в Request DTO)
```java
@Schema(description = "Запрос на создание покупателя")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HBBuyerRequest {
    
    @Schema(description = "ID покупателя (только для обновления)", example = "1")
    private Long id;
    
    @NotBlank(message = "Наименование обязательно")
    @Size(min = 3, max = 200, message = "Наименование должно быть от 3 до 200 символов")
    @Schema(description = "Наименование покупателя", example = "ОсОО Нефтегаз Плюс")
    private String name;
    
    @ValidInn // Кастомная валидация ИНН
    @Schema(description = "ИНН покупателя", example = "02010199612345")
    private String inn;
    
    @Email(message = "Некорректный email")
    @Schema(description = "Электронная почта", example = "buyer@example.com")
    private String email;
    
    @Pattern(regexp = "^\\+996\\d{9}$", message = "Некорректный номер телефона")
    @Schema(description = "Номер телефона", example = "+996555987654")
    private String phone;
}
```

### Транзакции
```java
@Transactional(rollbackFor = Exception.class)
public void complexOperation() {
    // Множественные операции с БД
}

@Transactional(readOnly = true)
public List<Entity> findAll() {
    // Только чтение данных
}
```

## 📦 Модули и их назначение
- **main**: Главный модуль с точкой входа FuelEcosystemApplication
- **shared**: Общие компоненты, утилиты, базовые классы
- **support-system**: Система техподдержки (тикеты, SLA, техники)
- **oil-depot**: Управление нефтебазой (HB префикс для Хайбек)
- **gas-station**: Управление АЗС
- **notification**: Система уведомлений

При работе всегда учитывать модульную структуру и переиспользовать компоненты из shared модуля.