package controllers;

public class Auth extends Controller {

    private final FormFactory formFactory;

    @Inject
    public Auth(FormFactory formFactory) {
        this.formFactory = formFactory;
    }

    //добавлять методы-контроллеры будем сюда
    // МАРШРУТЫ В conf/routes  !!!
}

    /**
     * Страница выдает пустую форму регистрации.
     * Если осуществлен вход(логин) перенаправляет на гл. страницу
     * @return пустая форма регистрации.
     */
    public Result signup() {
        if (session("email") != null) return redirect(routes.HomeController.index());
        else return ok(register.render(formFactory.form(Register.class)));
    }

    /**
     * Обработка формы регистрации.
     * Производится валидация формы. Штатными средствами класса Register
     *
     * Проверяется, что данный email еще не зарегистрирован в системе.
     *
     *
     * @return В случае успеха создает пользоваетеля в базе, аутентифицирует и перенаправляет на гл. страницу.
     */
    public Result register() {
        Form<Register> regForm = Form.form(Register.class).bindFromRequest();
        if (regForm.hasErrors()) return badRequest(register.render(regForm));  // валидация формы не прошла
        else {
            //Из формы достаем объект класса Register, нашу модель формы регистрации с заполненными полями
            Register reg = regForm.get();
            // В конструкторе пользователя производится установка хэша от пароля с солью
            User user = new User(reg.email, reg.password);
            // Сохраняем в БД
            user.save();
            // аутентифицируем пользоваетеля, устанавливаем ему ключ в сессии
            session("email", reg.email);
            // перенаправляем куда нам нужно после успешной аутентификации
            return redirect(routes.HomeController.index());
        }
    }


    /**
     * Страница выдачи формы для аутентификации
     * @return страница с пустой формой логин/пароль.
     *
     * NB: В случае, если пользователь уже аутентифицирован должна возвращать редирект на Application.index()
     *
     * Подсказка: воспользоваться генераторами форм на базе класса Login
     * Form.form(Login.class)
     *
     */
    public Result login() {
        if (session("email") != null) return redirect(routes.HomeController.index());
        else return ok(login.render(formFactory.form(Login.class)));
    }

    /**
     * Обработка формы аутентификации
     * Производит валидацию штатными средствами засчет Login.validate(). Он неявно вызывается при вызове
     *
     * loginForm.hasErrors()
     *
     * Подсказка: этот метод уже реализован для демонстрации.
     *
     * @return форму с ошибкой в случае не корректной валидации.
     * В случае успеха устанавливает ключ в сессии и перенаправляет на главную страницу
     */
    public Result auth() {
        Form<Login> loginForm = formFactory.form(Login.class).bindFromRequest();
        if (loginForm.hasErrors())
            //Ты не пройдешь!
            return badRequest(login.render(loginForm));
        else {
            //Пароль подошел. Устанавливаем ключ в сессии
            session("email", loginForm.get().email);
            //ключик в краткосрочной памяти длявыдачи alert-ов
            flash("success","Вы успешно аутентифицировались. Добро пожаловать!");
            //перенаправляем на вход либо на главную страницу, либо в область администрирования
            return redirect(routes.HomeController.index());
        }
    }

