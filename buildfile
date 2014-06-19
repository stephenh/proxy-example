
VERSION_NUMBER = "1.0.0"
GROUP = "proxy-example"

# Specify Maven 2.0 remote repositories here, like this:
repositories.remote << "http://repo1.maven.org/maven2"

desc "The Proxy-example project"
define "proxy-example" do
  project.version = VERSION_NUMBER
  project.group = GROUP
  compile.with 'com.esotericsoftware.kryo:kryo:jar:2.24.0'
  package(:jar)
end
