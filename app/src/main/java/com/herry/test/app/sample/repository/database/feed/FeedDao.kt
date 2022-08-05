package com.herry.test.app.sample.repository.database.feed

import androidx.room.Dao
import androidx.room.Query

@Dao
interface FeedDao {
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    fun insert(feed: Feed)
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    fun insertAll(feeds: List<Feed>)
//
//    @Update
//    fun update(feed: Feed)

    @Query("DELETE from feed")
    fun deleteAll()

    @Query(
        "SELECT * " +
                "FROM feed " +
                "WHERE project_id IN (:projectIds) " +
                "ORDER BY published_at DESC"
    )
    fun getList(projectIds: List<String>): List<Feed>

    @Query(
        "SELECT * " +
                "FROM feed " +
                "WHERE (CASE WHEN :category > 0 THEN category = :category ELSE category END) " +
                    "AND (CASE WHEN :lastProjectId='' THEN published_at < datetime('now') ELSE published_at < (SELECT published_at FROM feed WHERE project_id=:lastProjectId) END) " +
                "ORDER BY published_at DESC " +
                "LIMIT CASE WHEN :loadSize > 0 THEN :loadSize ELSE -1 END"
    )
    fun getList(category: Int, lastProjectId: String, loadSize: Int): List<Feed>

    @Query(
        "SELECT * " +
                "FROM (SELECT * FROM feed WHERE category ORDER BY published_at DESC LIMIT 30) " +
                "WHERE CASE WHEN :lastProjectId='' THEN published_at < datetime('now') ELSE published_at < (SELECT published_at FROM feed WHERE project_id=:lastProjectId) END " +
                "ORDER BY published_at DESC " +
                "LIMIT CASE WHEN :loadSize > 0 THEN :loadSize ELSE -1 END"
    )
    fun getNewFeeds(lastProjectId: String, loadSize: Int): List<Feed>

    @Query(
        "SELECT * " +
                "FROM feed " +
                "WHERE tags LIKE :tag " +
                    "AND (CASE WHEN :lastProjectId='' THEN published_at < datetime('now') ELSE published_at < (SELECT published_at FROM feed WHERE project_id=:lastProjectId) END) " +
                "ORDER BY published_at DESC " +
                "LIMIT CASE WHEN :loadSize > 0 THEN :loadSize ELSE -1 END"
    )
    fun getTagFeeds(tag: String, lastProjectId: String, loadSize: Int): List<Feed>

    @Query(
        "WITH RECURSIVE " +
            "tags_split(tags, str) AS (" +
                "SELECT '', replace(tags, ' ', '') || '#' FROM feed " +
                "UNION ALL SELECT " +
                "substr(str, 0, instr(str, '#'))," +
                "substr(str, instr(str, '#')+1) " +
                "FROM tags_split " +
                "WHERE str!='' and str!='\b'" +
            ") " +
        "SELECT DISTINCT tags " +
            "FROM tags_split " +
            "WHERE tags like :keyword " +
            "ORDER BY tags ASC; "
    )
    fun getAutoCompleteKeywords(keyword: String): List<String>

    /**
     * INNER JOIN : 두 테이블에 각각 조인이 되는 컬럼을 지정하고 각각의 컬럼에 같은 값이 저장되어 있는 데이터를 조인하여 받아온다
     * query:
     * SELECT 테이블명. 컬럼명, ...FROM 테이블명1
        INNER JOIN 테이블명2
        ON 테이블명1.컬럼명1 = 테이블명2. 컬럼명2;

     *
     */
}