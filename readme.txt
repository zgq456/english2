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

3.用户可以添加单词意思 UNKNOWN

统计用户学习记录
标识文章已完成

用户推荐单词
文章解析完后提醒

自由测试、正式测试 标出用户喜欢的句子

句子发音

提醒用户： 文章解析完成，新测试发布，每日学习汇总



 {
	 "button": [
     {
           "name":"功能菜单",
           "sub_button":[
           {	
               "type":"view",
               "name":"素材管理",
               "url":"https://open.weixin.qq.com/connect/oauth2/authorize?appid=wxad1e1211d18cd79d&redirect_uri=http%3a%2f%2fenglish.tiger.mopaas.com%2fhtml%2fajax%2fajaxArticles.html&response_type=code&scope=snsapi_userinfo&state=123#wechat_redirect"
            },
            {
               "type":"view",
               "name":"测验管理",
               "url":"https://open.weixin.qq.com/connect/oauth2/authorize?appid=wxad1e1211d18cd79d&redirect_uri=http%3a%2f%2fenglish.tiger.mopaas.com%2fhtml%2fajax%2fajax.html&response_type=code&scope=snsapi_userinfo&state=123#wechat_redirect"
            } ]
       }]
 }
 
 {
	 "button": [
     {
           "name":"我的菜单",
           "sub_button":[
           {	
               "type":"view",
               "name":"素材管理",
               "url":"https://open.weixin.qq.com/connect/oauth2/authorize?appid=wxad1e1211d18cd79d&redirect_uri=http%3a%2f%2fenglish.tiger.mopaas.com%2fhtml%2fajax%2fajaxArticles.html&response_type=code&scope=snsapi_userinfo&state=123#wechat_redirect"
            },
            {
               "type":"view",
               "name":"测验管理",
               "url":"https://open.weixin.qq.com/connect/oauth2/authorize?appid=wxad1e1211d18cd79d&redirect_uri=http%3a%2f%2fenglish.tiger.mopaas.com%2fhtml%2fajax%2fajax.html&response_type=code&scope=snsapi_userinfo&state=123#wechat_redirect"
            },
            {
               "type":"view",
               "name":"我的单词",
               "url":"https://open.weixin.qq.com/connect/oauth2/authorize?appid=wxad1e1211d18cd79d&redirect_uri=http%3a%2f%2fenglish.tiger.mopaas.com%2fhtml%2fajax%2fajaxMyWords.html&response_type=code&scope=snsapi_userinfo&state=123#wechat_redirect"
            },
            {
               "type":"view",
               "name":"我的句子",
               "url":"https://open.weixin.qq.com/connect/oauth2/authorize?appid=wxad1e1211d18cd79d&redirect_uri=http%3a%2f%2fenglish.tiger.mopaas.com%2fhtml%2fajax%2fajaxMySens.html&response_type=code&scope=snsapi_userinfo&state=123#wechat_redirect"
            } ,
            {
               "type":"view",
               "name":"复习单词",
               "url":"https://open.weixin.qq.com/connect/oauth2/authorize?appid=wxad1e1211d18cd79d&redirect_uri=http%3a%2f%2fenglish.tiger.mopaas.com%2fhtml%2fajax%2fajaxMyFreeQuiz.html&response_type=code&scope=snsapi_userinfo&state=123#wechat_redirect"
            }]
       },
       {
           "name":"他山之石",
           "sub_button":[
           {	
               "type":"view",
               "name":"别人单词",
               "url":"https://open.weixin.qq.com/connect/oauth2/authorize?appid=wxad1e1211d18cd79d&redirect_uri=http%3a%2f%2fenglish.tiger.mopaas.com%2fhtml%2fajax%2fajaxOthersWords.html&response_type=code&scope=snsapi_userinfo&state=123#wechat_redirect"
            }, {	
               "type":"view",
               "name":"别人素材",
               "url":"https://open.weixin.qq.com/connect/oauth2/authorize?appid=wxad1e1211d18cd79d&redirect_uri=http%3a%2f%2fenglish.tiger.mopaas.com%2fhtml%2fajax%2fajaxOthersArticles.html&response_type=code&scope=snsapi_userinfo&state=123#wechat_redirect"
            }]
       },
       {
           "name":"设置管理",
           "sub_button":[
           {	
               "type":"view",
               "name":"提醒设置",
               "url":"https://open.weixin.qq.com/connect/oauth2/authorize?appid=wxad1e1211d18cd79d&redirect_uri=http%3a%2f%2fenglish.tiger.mopaas.com%2fhtml%2fajax%2fajaxReminder.html&response_type=code&scope=snsapi_userinfo&state=123#wechat_redirect"
            }]
       }]
 }
 
 
 
 --统计用户今日所学 以及 净学
 select * from (
	SELECT user_id, word_id FROM english2.quiz_result where user_id = 7 
and last_upt > '2015-05-11' and is_right = 1
group by user_id, word_id
) a left join (
SELECT user_id, word_id, count(*) FROM english2.quiz_result 
where user_id = 7 group by user_id, word_id and last_upt < '2015-05-11' 
) b on a.word_id = b.word_id;
