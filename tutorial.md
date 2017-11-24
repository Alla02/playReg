# Инструкции

См. документацию по Playframework https://www.playframework.com/documentation/latest/Home для подробной информации.

## Подключение базы данных 
В build.sbt добавьте следующие строки

```scala
libraryDependencies += javaJdbc
libraryDependencies += "com.h2database" % "h2" % "1.4.194"
```

Первая строка подключает драйвер JDBC. Вторая строка подключает необходимый драйвер СУБД H2. 

В файле conf/application.conf раскомментируйте следующие строки:
```
db {
  # https://www.playframework.com/documentation/latest/Developing-with-the-H2-Database
  default.driver = org.h2.Driver
  default.url = "jdbc:h2:mem:play"
  default.username = sa
  default.password = ""
}

```
Решетка # - символ комментария. Здесь мы подключаем драйвер и прописываем настройки для БД с именем default.

# Ebean
В build.sbt добавьте плагин PlayEbean

```scala
lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean)
```
В project/plugins.sbt добавьте плагин

```scala
addSbtPlugin("com.typesafe.sbt" % "sbt-play-ebean" % "4.0.2")
```
это даст инструкцию сборщику разрешить зависимости и скачать необходимые библиотеки и подключить их к проекту.

## Зависимости
Зайдите в консоль sbt 

```bash
sbt
```
и введите команды
```sbtshell
update
compile
```
Первая команда обновит зависимости. Вторая команда компилирует. После этого у вас будут в проекте необходимые библиотеки. 

# Модели и настройка ORM
Создайте пакет models  и в нем создайте класс Feature
```java
import java.util.*;
import javax.persistence.*;

import io.ebean.*;
import play.data.format.*;
import play.data.validation.*;

@Entity
public class Feature extends Model {

    @Id
    public Long id;

    @Constraints.Required
    public String title;

    public String description;

    public static final Finder<Long, Feature> find = new Finder<>(Feature.class);
}

```
Для того чтобы указать ORM, что мы хотим все наши классы из пакета models отображать в таблицы БД H2 с помощью Ebean, 
укажем это в конфигурации.

В файле conf/application.conf добавьте следующие строки:
```properties
ebean.default = ["models.*"]
```

##Долгосрочное хранение
Если вы хотите настроить вашу H2 для хранения данных не в памяти, а в файле, необходимо указать следующую строчку в conf/application.conf
```properties
default.url = "jdbc:h2:file:./db/play"
```


# Bootstrap для полей формы
Для того, чтобы определить собственную отрисовку полей формы (например, в стиле Bootstrap), 
можно определять собственные функции в собственных пакетах. Для начала создадим папку app/views/bootstrap
В ней определим шаблон для текстового поля в соответствии с документацией Twitter Bootstrap:

```scala
@**
* Генерирует HTML input.
*
* Пример:
* {{{
* @text(field = myForm("name"), args = 'size -> 10, 'placeholder -> "Your name")
* }}}
*
* @param Поле для формы.
* @param args Множество дополнительных атрибутов.
* @param handler конструктор поля.
*@
@(field: play.api.data.Field, args: (Symbol,Any)*)(implicit handler: helper.FieldConstructor, lang: play.api.i18n.Lang)

@import helper._;

@inputType = @{ args.toMap.get('type).map(_.toString).getOrElse("text") }
@invalid = @{if (field.hasErrors){"is-invalid"} else if(!field.value.isEmpty){"is-valid"} else {""}}

  <div class="form-group @if(field.hasErrors){error}">
    <label class="control-label" for="@field.id">@{args.toMap.get('_label).map(_.toString).getOrElse(field.name)}</label>

    <input class="form-control @invalid" type="@inputType" id="@field.id" name="@field.name" value="@field.value" @toHtmlArgs(args.filter(arg => !arg._1.name.startsWith("_") && arg._1 != 'id).toMap) />
    <div class="invalid-feedback">@{field.error.map { error => error.message }}</div>
    <div class="help-block">@{args.toMap.get('_help).map(_.toString).getOrElse("")}</div>

  </div>
```

Далее используем нашу новую функцию, нашу новую компоненту для отрисовки поля формы вместо  стандартоного inputText 
в createForm.scala.html получим:

```scala
@(featureForm: Form[Feature])

@import helper._
@import bootstrap._

@main {
    
    <h1>Новая фича</h1>
    
    @form(routes.HomeController.save()) {
        
        <fieldset>
            @CSRF.formField
            @text(featureForm("title"), '_label -> "Название", '_help -> "")
            @text(featureForm("description"), '_label -> "Описание", '_help -> "")
            @text(featureForm("imageUrl"), '_label -> "Ссылка на картинку", '_help -> "")
        </fieldset>
        
        <div class="actions">
            <input type="submit" value="Создать фичу" class="btn btn-primary"> или
            <a href="@routes.HomeController.list()" class="btn btn-default">Отменить</a>
        </div>
    }
}
```

Для того, чтобы определить textarea для ввода большого количество текста, аналогичным образом определить функцию-шаблон для textarea в пакете bootstrap 
(app\views\bootstrap\textareea.scala.html) 
```scala
@**
* Generate an HTML input text.
*
* Example:
* {{{
* @inputText(field = myForm("name"), args = 'size -> 10, 'placeholder -> "Your name")
* }}}
*
* @param field The form field.
* @param args Set of extra attributes.
* @param handler The field constructor.
*@
@(field: play.api.data.Field, args: (Symbol,Any)*)(implicit handler: helper.FieldConstructor, lang: play.api.i18n.Lang)

@import helper._;
@invalid = @{if (field.hasErrors){"is-invalid"} else if(!field.value.isEmpty){"is-valid"} else {""}}

  <div class="form-group">
    <label class="control-label" for="@field.id">@{args.toMap.get('_label).map(_.toString).getOrElse(field.name)}</label>
    <textarea class="form-control @invalid" id="@field.id" name="@field.name" @toHtmlArgs(args.filter(arg => !arg._1.name.startsWith("_") && arg._1 != 'id).toMap)>@field.value</textarea>
    <div class="invalid-feedback">
      @{field.error.map { error => error.message }}
    </div>
    <div class="help-block">@{args.toMap.get('_help).map(_.toString).getOrElse("")}</div>
  </div>

```

Для того, чтобы вместо error.required выдавало осмысленный текст, необходимо для полей задавать
параметр message валидации:
```java
    @Constraints.Required(message = "Обязательное поле")
    public String description;
```

Теперь можно запустить проект. При этом вам предложат создать БД, если она создается впервые.

## Запуск
Запуск с помощью [sbt](http://www.scala-sbt.org/).  

```
sbt run
```

В компьютерных классах ИГУ, используйте файл с настройками прокси-сервера для запуска

```
sbt-isu run
``` 

И затем откройте http://localhost:9000 в браузере.

