mvn package -Dmaven.test.skip=true

export M2_HOME=/home/zgq/apache-maven-3.3.1

/home/zgq/apache-maven-3.3.1/bin/mvn compile
/home/zgq/apache-maven-3.3.1/bin/mvn package -Dmaven.test.skip=true

mvn clean compile package install -Denv=prod -Dmaven.test.skip=true


http://96.126.126.117:8080/html/ajax/ajax.html

tail -f english2/target/log.txt




http://www.manythings.org/vocabulary/lists/l/

http://www.englishpage.com/irregularverbs/irregularverbs.html
https://www.englishclub.com/vocabulary/irregular-verbs-list.htm
http://www.usingenglish.com/reference/irregular-verbs/

TODO:
1.收藏文章
SELECT a.id, a.name, a.type, a.url, a.open u.email,  uafa.article_id, a.*,  count(*) folk, case when uafa.user_id = 1 then 999999999 else uafa.user_id end  user_id2 
FROM english2.user_article_fork_asso  uafa, article a, user u
where uafa.article_id = a.id and (open_flag = 'Yes' or uafa.user_id = 1)  and a.delete_flag = 0 and u.id = uafa.user_id
group by uafa.article_id order by user_id2 desc, folk desc, last_upt desc;

2.索引
create index for some columns.

3.用户可以纠错单词
