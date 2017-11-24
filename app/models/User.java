package models;

@Entity
public class User extends Model {

    /**
     * Email - электронная почта пользователя
     */
    @Id
    @Email
    private String email;

    /**
     * Хэш пароля
     */
    private String passwordHash;

    /**
     * Соль, случайная последовательность символов, используется для хэширования пароля ,
     * хранится для каждого пользователя своя.
     */
    private String salt;

    public User(String email, String password) {
        this.email = email;
        setPassword(password);
    }

    /**
     * Объект для связи с СУБД. Для поиска объектов в базе данных
     */
    public static Finder<String, User> find = new Finder<String, User>(User.class);

}

    /**
     * Подписывает строку СЕКРЕТОМ приложения с помощью встроенной в Play библиотекой Crypto
     * Используется алгоритм SHA-1
     *
     * @link play.api.libs.Crypto.sign
     * @see play.api.libs.Crypto.cookieSigner().sign(String)
     *
     * @param str Строка для подписи (хэширования)
     * @return подпись, хэш для заданной строки
     */
    public static String SHA1(String str){
        //Используется класс Crypto из пакета, встроенного в Play
        return Crypto.cookieSigner().sign(str);
    }

    /**
     * Генерирует хэш от строки.
     * Определяет текущую версию выбранного алгоритма конкретной криптосистемы
     *
     * @param s строка
     * @return хэш от пароля в соответствии с применяемым алгоритмом
     */
    private String getHash(String s) {
        return SHA256(s);
    }

    /**
     * Возвращает хэш-функцию SHA-256
     *
     * @see <a href="https://ru.wikipedia.org/wiki/SHA-2">SHA-2 в Википедии</a>
     *
     * @param str строка для хэширования
     * @return хэш от строки
     */
    public static String SHA256(String str) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(str.getBytes());
            return Base64.encodeBase64String(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Генерирует случайную строку для использования в качестве соли
     * @return случайная последовательность символов
     */
    public static String genSalt(){
        final Random r = new SecureRandom();
        byte[] salt = new byte[32];
        r.nextBytes(salt);
        return Base64.encodeBase64String(salt);
    }

    /**
     * Устанавливаем новый пароль для пользователя.
     *
     * @param password (новый) пароль
     */
    public void setPassword(String password) {
        //Сгенерировать соль
        this.salt = genSalt();
        //установить хэш от пароля (с солью)
        this.passwordHash = getHash(password+salt);
    }

    /**
     * Проверяем подошел ли пароль
     *
     * @param password пароль
     * @return в случае совпадения пароля, возвращет true, иначе возвращает false
     */
    private boolean checkPassword(String password) {
        return getHash(password+salt).equals(passwordHash);
    }

    /**
     * @param email почтовый адрес
     * @param password пароль
     * @return возвращает null в случае успешной аутентификации.
     * В случае если пользователь не зарегистрирован возвращает сообщение об ошибке
     * "Пользователь с данным email не зарегистрирован или не верный пароль"
     */
    public static String authenticate(String email, String password) {
        User user = find.byId(email);
        if (user == null || !user.checkPassword(password))
            return "Пользователь с данным email не зарегистрирован или не верный пароль";
        else
            return null;
    }