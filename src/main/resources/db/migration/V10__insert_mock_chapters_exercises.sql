-- V10: Mock chapters and exercises

# DELETE FROM chapter_exercise;
# DELETE FROM skill_chapter;

INSERT INTO skill_chapter (skill_id, title, content, sort_order, status) VALUES (1, 'Java 基础语法', '## Java 基础语法

变量、数据类型、控制流', 1, 'PUBLISHED');
INSERT INTO chapter_exercise (chapter_id, question, options, answer, explanation, sort_order) VALUES ((SELECT id FROM (SELECT id FROM skill_chapter WHERE skill_id=1 AND sort_order=1) t), 'Java 中哪个关键字用于定义一个类？', '["function","class","def","struct"]', 1, 'Java 使用 class 关键字定义类。', 1);
INSERT INTO chapter_exercise (chapter_id, question, options, answer, explanation, sort_order) VALUES ((SELECT id FROM (SELECT id FROM skill_chapter WHERE skill_id=1 AND sort_order=1) t), '以下哪个不是 Java 的基本数据类型？', '["int","boolean","String","double"]', 2, 'String 是引用类型。', 2);
INSERT INTO chapter_exercise (chapter_id, question, options, answer, explanation, sort_order) VALUES ((SELECT id FROM (SELECT id FROM skill_chapter WHERE skill_id=1 AND sort_order=1) t), 'Java 中 System.out.println() 的作用是？', '["读取输入","输出到控制台并换行","输出到文件","计算表达式"]', 1, '输出内容并换行。', 3);

INSERT INTO skill_chapter (skill_id, title, content, sort_order, status) VALUES (1, 'Java 面向对象编程', '## Java 面向对象编程

封装、继承、多态、抽象类、接口', 2, 'PUBLISHED');
INSERT INTO chapter_exercise (chapter_id, question, options, answer, explanation, sort_order) VALUES ((SELECT id FROM (SELECT id FROM skill_chapter WHERE skill_id=1 AND sort_order=2) t), 'Java 中实现继承使用哪个关键字？', '["implements","extends","inherits","super"]', 1, 'extends 实现继承。', 1);
INSERT INTO chapter_exercise (chapter_id, question, options, answer, explanation, sort_order) VALUES ((SELECT id FROM (SELECT id FROM skill_chapter WHERE skill_id=1 AND sort_order=2) t), '关于多态，正确的是？', '["Java支持多继承","多态只包括重写","多态包括重载和重写","static方法可被重写"]', 2, '多态包括重载和重写。', 2);
INSERT INTO chapter_exercise (chapter_id, question, options, answer, explanation, sort_order) VALUES ((SELECT id FROM (SELECT id FROM skill_chapter WHERE skill_id=1 AND sort_order=2) t), '关于抽象类和接口，错误的是？', '["抽象类可有构造方法","接口方法默认public abstract","抽象类可实现接口","接口可有构造方法"]', 3, '接口不能有构造方法。', 3);

INSERT INTO skill_chapter (skill_id, title, content, sort_order, status) VALUES (1, 'Java 集合框架', '## Java 集合框架

List、Set、Map、HashMap', 3, 'PUBLISHED');
INSERT INTO chapter_exercise (chapter_id, question, options, answer, explanation, sort_order) VALUES ((SELECT id FROM (SELECT id FROM skill_chapter WHERE skill_id=1 AND sort_order=3) t), 'ArrayList 和 LinkedList 的区别？', '["ArrayList基于链表","LinkedList查询更快","ArrayList随机访问更快","LinkedList不支持随机访问"]', 2, 'ArrayList 基于数组，随机访问 O(1)。', 1);
INSERT INTO chapter_exercise (chapter_id, question, options, answer, explanation, sort_order) VALUES ((SELECT id FROM (SELECT id FROM skill_chapter WHERE skill_id=1 AND sort_order=3) t), 'HashMap 在 JDK8 中的底层实现？', '["纯数组","数组+链表","数组+链表+红黑树","纯红黑树"]', 2, 'JDK8 采用数组+链表+红黑树。', 2);
INSERT INTO chapter_exercise (chapter_id, question, options, answer, explanation, sort_order) VALUES ((SELECT id FROM (SELECT id FROM skill_chapter WHERE skill_id=1 AND sort_order=3) t), '以下哪个集合类是线程安全的？', '["ArrayList","HashMap","HashSet","ConcurrentHashMap"]', 3, 'ConcurrentHashMap 线程安全。', 3);

INSERT INTO skill_chapter (skill_id, title, content, sort_order, status) VALUES (4, 'Python 基础语法', '## Python 基础语法

变量、列表、字典、控制流', 1, 'PUBLISHED');
INSERT INTO chapter_exercise (chapter_id, question, options, answer, explanation, sort_order) VALUES ((SELECT id FROM (SELECT id FROM skill_chapter WHERE skill_id=4 AND sort_order=1) t), 'Python 中定义变量需要指定类型吗？', '["需要","不需要","只有字符串需要","只有数字需要"]', 1, 'Python 是动态类型语言。', 1);

INSERT INTO skill_chapter (skill_id, title, content, sort_order, status) VALUES (4, 'Python 函数与模块', '## Python 函数与模块

函数定义、Lambda、内置函数', 2, 'PUBLISHED');
INSERT INTO chapter_exercise (chapter_id, question, options, answer, explanation, sort_order) VALUES ((SELECT id FROM (SELECT id FROM skill_chapter WHERE skill_id=4 AND sort_order=2) t), 'Python 中 lambda x: x*2 的作用？', '["定义名为lambda的函数","定义匿名函数x返回x*2","创建列表","语法错误"]', 1, 'lambda 定义匿名函数。', 1);

INSERT INTO skill_chapter (skill_id, title, content, sort_order, status) VALUES (5, 'HTML5 语义化标签', '## HTML5 语义化标签

header、nav、main、article、footer', 1, 'PUBLISHED');
INSERT INTO chapter_exercise (chapter_id, question, options, answer, explanation, sort_order) VALUES ((SELECT id FROM (SELECT id FROM skill_chapter WHERE skill_id=5 AND sort_order=1) t), 'HTML5 中定义导航区域的标签是？', '["<navigation>","<nav>","<menu>","<links>"]', 1, 'nav 定义导航区域。', 1);

INSERT INTO skill_chapter (skill_id, title, content, sort_order, status) VALUES (5, 'CSS3 Flexbox 布局', '## CSS3 Flexbox 布局

flex-direction、justify-content、align-items', 2, 'PUBLISHED');
INSERT INTO chapter_exercise (chapter_id, question, options, answer, explanation, sort_order) VALUES ((SELECT id FROM (SELECT id FROM skill_chapter WHERE skill_id=5 AND sort_order=2) t), 'Flexbox 中设置主轴方向的属性？', '["align-items","flex-direction","justify-content","flex-wrap"]', 1, 'flex-direction 设置主轴方向。', 1);

INSERT INTO skill_chapter (skill_id, title, content, sort_order, status) VALUES (6, 'JavaScript 基础语法', '## JavaScript 基础语法

变量声明、数据类型、函数', 1, 'PUBLISHED');
INSERT INTO chapter_exercise (chapter_id, question, options, answer, explanation, sort_order) VALUES ((SELECT id FROM (SELECT id FROM skill_chapter WHERE skill_id=6 AND sort_order=1) t), 'let 和 var 的主要区别？', '["let不能声明变量","let有块级作用域","var不能声明变量","没有区别"]', 1, 'let 有块级作用域。', 1);

INSERT INTO skill_chapter (skill_id, title, content, sort_order, status) VALUES (6, 'JavaScript ES6+ 新特性', '## JavaScript ES6+ 新特性

解构赋值、async/await、模块化', 2, 'PUBLISHED');
INSERT INTO chapter_exercise (chapter_id, question, options, answer, explanation, sort_order) VALUES ((SELECT id FROM (SELECT id FROM skill_chapter WHERE skill_id=6 AND sort_order=2) t), 'ES6 中处理异步的语法糖？', '["Promise","callback","async/await","setTimeout"]', 2, 'async/await 是 Promise 的语法糖。', 1);

INSERT INTO skill_chapter (skill_id, title, content, sort_order, status) VALUES (9, 'Spring Boot 快速入门', '## Spring Boot 快速入门

自动配置、REST API、配置文件', 1, 'PUBLISHED');
INSERT INTO chapter_exercise (chapter_id, question, options, answer, explanation, sort_order) VALUES ((SELECT id FROM (SELECT id FROM skill_chapter WHERE skill_id=9 AND sort_order=1) t), 'Spring Boot 核心特性不包括？', '["自动配置","起步依赖","内嵌服务器","手动配置XML"]', 3, 'Spring Boot 减少 XML 配置。', 1);

INSERT INTO skill_chapter (skill_id, title, content, sort_order, status) VALUES (9, 'MyBatis-Plus 数据访问', '## MyBatis-Plus 数据访问

实体注解、CRUD 操作', 2, 'PUBLISHED');
INSERT INTO chapter_exercise (chapter_id, question, options, answer, explanation, sort_order) VALUES ((SELECT id FROM (SELECT id FROM skill_chapter WHERE skill_id=9 AND sort_order=2) t), '@TableId(type=IdType.AUTO) 的作用？', '["设置主键","设置主键自增","设置字段名","设置默认值"]', 1, 'IdType.AUTO 表示自增主键。', 1);

INSERT INTO skill_chapter (skill_id, title, content, sort_order, status) VALUES (10, 'SQL 基础查询', '## SQL 基础查询

SELECT、聚合函数、多表查询', 1, 'PUBLISHED');
INSERT INTO chapter_exercise (chapter_id, question, options, answer, explanation, sort_order) VALUES ((SELECT id FROM (SELECT id FROM skill_chapter WHERE skill_id=10 AND sort_order=1) t), 'SQL 中过滤分组结果的关键字？', '["WHERE","HAVING","GROUP BY","ORDER BY"]', 1, 'HAVING 过滤分组结果。', 1);

INSERT INTO skill_chapter (skill_id, title, content, sort_order, status) VALUES (10, 'MySQL 索引优化', '## MySQL 索引优化

索引类型、B+树、索引失效', 2, 'PUBLISHED');
# INSERT INTO chapter_exercise (chapter_id, question, options, answer, explanation, sort_order) VALUES ((SELECT id FROM (SELECT id FROM skill_chapter WHERE skill_id=10 AND sort_order=2) t), '哪种查询会导致索引失效？', '["WHERE age=18","WHERE name LIKE "张%"","WHERE age+1>18","WHERE id=100"]', 2, '对索引列做运算导致索引失效。', 1);

