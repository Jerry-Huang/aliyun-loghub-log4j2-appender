# aliyun-loghub-log4j2-appender
Aliyun loghub appender on Log4j2

Quick Start
-------------
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xml>
<Configuration packages="com.aliyun.openservices.log.log4j">

	<Appenders>

		<Loghub name="LOGHUB"
		    projectName="<your project name in aliyun>"
		    logstore="<your logstore under project>"
		    endpoint="<project name>.<endpoint>"
		    accessKeyId="<accessKeyId>"
		    accessKey="<accessKey>"
		    timeZone="GMT+8"
		    timeFormat="yyyy-MM-dd HH:mm:ss,SSS">
		</Loghub>
	</Appenders>

	<Loggers>
		<AsyncRoot level="INFO">
			<AppenderRef ref="LOGHUB" />
		</AsyncRoot>
	</Loggers>
</Configuration>
```
[Endpoint reference](https://help.aliyun.com/document_detail/29008.html?spm=5176.2020520112.108.1.nEAIzi)
