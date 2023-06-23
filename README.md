# java-filmorate
Template repository for Filmorate project.

# Схема базы данных
![](https://github.com/DenisPolo/java-filmorate/blob/main/database_schema.svg)

# Примеры основных запросов из базы данных

## Получение всех фильмов:

```
SELECT  f.film_id,
	f.film_name,
	f.description,
	f.release,
	f.duration,
	mpa.mpa_id,
	mpa.mpa_name,
	fgj.genre_ids,
	fgj.genre_names,
	l.likes
FROM films AS f
LEFT OUTER JOIN mpa AS mpa ON f.mpa_id = mpa.mpa_id
LEFT OUTER JOIN (SELECT fg.film_id, 
		STRING_AGG (fg.genre_id, ', ') AS genre_ids,
		STRING_AGG (g.genre_name, ', ') AS genre_names
		FROM film_genres AS fg
		LEFT OUTER JOIN genres AS g ON fg.genre_id = g.genre_id
		GROUP BY fg.film_id) AS fgj ON f.film_id = fgj.film_id
LEFT OUTER JOIN (SELECT film_id,
		STRING_AGG (user_id, ', ') AS likes
		FROM likes
		GROUP BY film_id) AS l ON f.film_id = l.film_id
ORDER BY f.film_id ASC;
```

## Получение фильма с ID 1:

```
SELECT  f.film_id,
	f.film_name,
	f.description,
	f.release,
	f.duration,
	mpa.mpa_id,
	mpa.mpa_name,
	fgj.genre_ids,
	fgj.genre_names,
	l.likes
FROM films AS f
LEFT OUTER JOIN mpa AS mpa ON f.mpa_id = mpa.mpa_id
LEFT OUTER JOIN (SELECT fg.film_id, 
		STRING_AGG (fg.genre_id, ', ') AS genre_ids,
		STRING_AGG (g.genre_name, ', ') AS genre_names
		FROM film_genres AS fg
		LEFT OUTER JOIN genres AS g ON fg.genre_id = g.genre_id
		GROUP BY fg.film_id) AS fgj ON f.film_id = fgj.film_id
LEFT OUTER JOIN (SELECT film_id,
		STRING_AGG (user_id, ', ') AS likes
		FROM likes
		GROUP BY film_id) AS l ON f.film_id = l.film_id
WHERE f.film_id = 1;
```

## Получение ТОП 10 популярных фильмов:

```
SELECT  f.film_id,
	f.film_name,
	f.description,
	f.release,
	f.duration,
	mpa.mpa_id,
	mpa.mpa_name,
	fgj.genre_ids,
	fgj.genre_names,
	l.likes,
	l.likes_count
FROM films AS f
LEFT OUTER JOIN mpa AS mpa ON f.mpa_id = mpa.mpa_id
LEFT OUTER JOIN (SELECT fg.film_id, 
		STRING_AGG (fg.genre_id, ', ') AS genre_ids,
		STRING_AGG (g.genre_name, ', ') AS genre_names
		FROM film_genres AS fg
		LEFT OUTER JOIN genres AS g ON fg.genre_id = g.genre_id
		GROUP BY fg.film_id) AS fgj ON f.film_id = fgj.film_id
LEFT OUTER JOIN (SELECT film_id,
		STRING_AGG (user_id, ', ') AS likes,
		COUNT (user_id) AS likes_count
		FROM likes
		GROUP BY film_id) AS l ON f.film_id = l.film_id
ORDER BY l.likes_count DESC, f.film_id ASC
LIMIT 10;
```



## Получение всех пользователей:

```
SELECT  u.user_id,
	u.email,
	u.login,
	u.user_name,
	u.birthday,
	STRING_AGG (f.friend_id, ', ') AS friends
FROM users AS u
LEFT OUTER JOIN friends AS f ON u.user_id = f.user_id
GROUP BY u.user_id
ORDER BY u.user_id ASC;
```

## Получение пользователя с ID 1:

```
SELECT  u.user_id,
	u.email,
	u.login,
	u.user_name,
	u.birthday,
	STRING_AGG (f.friend_id, ', ') AS friends
FROM users AS u
LEFT OUTER JOIN friends AS f ON u.user_id = f.user_id
WHERE u.user_id = 1
GROUP BY u.user_id;
```

## Получение друзей пользователz с ID 1:

```
SELECT  u.user_id,
	u.email,
	u.login,
	u.user_name,
	u.birthday,
	STRING_AGG (f.friend_id, ', ') AS friends
FROM users AS u
LEFT OUTER JOIN friends AS f ON u.user_id = f.user_id
WHERE u.user_id IN (SELECT  friend_id AS user_id
	FROM friends
	WHERE user_id = 1)
GROUP BY u.user_id
ORDER BY u.user_id ASC;
```

## Получение общих друзей пользователей с ID 1 и ID 2:

```
SELECT  u.user_id,
	u.email,
	u.login,
	u.user_name,
	u.birthday,
	STRING_AGG (f.friend_id, ', ') AS friends
FROM users AS u
LEFT OUTER JOIN friends AS f ON u.user_id = f.user_id
WHERE u.user_id IN (SELECT  f1.friend_id
	FROM friends AS f1
	INNER JOIN (SELECT friend_id FROM friends WHERE user_id = 2) AS f2 ON f1.friend_id = f2.friend_id
	WHERE f1.user_id = 1)
GROUP BY u.user_id
ORDER BY u.user_id ASC;
```