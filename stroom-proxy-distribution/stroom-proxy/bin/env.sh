ulimit -v unlimited
 
export MALLOC_ARENA_MAX=4

export JAVA_OPTS="${JAVA_OPTS} -Djava.awt.headless=true -Xms512m -Xmx1g -XX:PermSize=256m -XX:MaxPermSize=512m @@JAVA_OPTS@@"
