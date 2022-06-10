## Search Engine

### Crawler service

**Other services:**

- [**`crawler`**](https://github.com/Wildcall/search_engine/tree/master/crawler) <
- [**`indexer`**](https://github.com/Wildcall/search_engine/tree/master/indexer)
- [**`searcher`**](https://github.com/Wildcall/search_engine/tree/master/searcher)
- [**`task`**](https://github.com/Wildcall/search_engine/tree/master/task_manager)
- [**`notification`**](https://github.com/Wildcall/search_engine/tree/master/notification)
- [**`registration`**](https://github.com/Wildcall/search_engine/tree/master/registration)

**Build:**

```
cd path_to_project
docker-compose up
mvn clean package repack
```

**Running:**
```
java -jar -DDATABASE_URL=postgresql://localhost:5430/se_crawler_data -DDATABASE_USER=crawler_user -DDATABASE_PASS=crawler_password -DCRAWLER_SECRET=CRAWLER_SECRET -DTASK_MANAGER_SECRET=TASK_MANAGER_SECRET
```

**Environment Variable:**

- `DATABASE_URL` postgresql://localhost:5430/se_crawler_data
- `DATABASE_USER` crawler_user
- `DATABASE_PASS` crawler_password
- `CRAWLER_SECRET`
- `TASK_MANAGER_SECRET`

**Api:**

- api/v1
- api/v1/crawler/start
- api/v1/crawler/stop
- api/v1/api/v1/page?siteId={siteId}&appUserId={appUserId}
- api/v1/sse/crawler
- api/v1/sse/page