# java-filmorate
Template repository for Filmorate project.

# Схема базы данных
![](https://github.com/DenisPolo/java-filmorate/blob/main/database_schema.svg)

# Примеры основных запросов из базы данных

## Получение всех фильмов:

```
SELECT  f.film_id,
        f.name,        
        f.description,        
        f.release,        
        f.duration,        
        g.genre_name AS genre,        
        r.rating_name AS rating,        
        l.likes        
FROM    (
        SELECT  film_id,        
        COUNT(user_id) AS likes        
        FROM likes        
        GROUP BY film_id        
        ORDER BY film_id ASC        
        ) AS l        
RIGHT OUTER JOIN film AS f ON l.film_id = f.film_id
LEFT OUTER JOIN genre AS g ON f.genre_id = g.genre_id
LEFT OUTER JOIN rating AS r ON f.rating_id = r.rating_id
ORDER BY f.film_id ASC;
```



## Получение ТОП 10 популярных фильмов:

```
SELECT  f.film_id,
        f.name,
        f.description,
        f.release,
        f.duration,
        COUNT(fg.genre_id) AS amount_genre,
        r.rating_name AS rating,
        l.likes
FROM    (
        SELECT  film_id,
                COUNT(user_id) AS likes
        FROM likes
        GROUP BY film_id
        ORDER BY likes DESC
        LIMIT 10
        ) AS l
LEFT OUTER JOIN film AS f ON l.film_id = f.film_id
LEFT OUTER JOIN film_genre AS fg ON f.film_id = fg.film_id
LEFT OUTER JOIN rating AS r ON f.rating_id = r.rating_id
ORDER BY l.likes DESC;
```



## Получение всех пользователей:

```
SELECT  u.user_id,
        u.email,        
        u.login,        
        u.name,        
        u.birthday,        
        COUNT(f.friends_id) AS friends        
FROM user AS u
LEFT OUTER JOIN friends AS f ON u.user_id = f.user_id
LEFT OUTER JOIN status AS s ON f.status_id = s.status_id
WHERE status_name = ‘friend’
GROUP BY u.user_id
ORDER BY u.user_id ASC;
```



## Получение общих друзей пользователей с ID 1 и ID 2:

```
SELECT  u.user_id,
        u.email,        
        u.login,        
        u.name,        
        u.birthday,        
        COUNT(f.friends_id) AS friends        
FROM (
    SELECT friend_id    
    FROM friends    
    WHERE user_id = 1) AS f1    
INNER JOIN (
    SELECT friend_id    
    FROM friends    
    WHERE user_id = 2) AS f2    
LEFT OUTER JOIN friends AS f ON f2.friend_id = f.user_id
LEFT OUTER JOIN status AS s ON f.status_id = s.status_id
LEFT OUTER JOIN user AS u ON f2.friend_id = u.user_id
WHERE status_name = ‘friend’
GROUP BY f2.friend_id
ORDER BY u.user_id ASC;
```
