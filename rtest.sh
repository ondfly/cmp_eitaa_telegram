#Follow the steps rather than running the script
#Steps 4 and 5 are supposed to be done manually!

##1) get apk files
#eitaa apk:
wget https://eitaa.com/dl/android -O eitaa.apk 

#Telgram apk: 
#https://apkpure.com/telegram/org.telegram.messenger >telegram.apk
#Telegram source code:
wget https://github.com/DrKLO/Telegram/archive/master.zip -O telegram.zip
unzip -x telegram.zip -d telegram-src

##2) convert apk file to jar file
#dex2jar convertor:
wget https://sourceforge.net/projects/dex2jar/files/dex2jar-2.0.zip/download -O dex2jar.zip
unzip -x dex2jar-2.0.zip && mv dex2jar-2.0 dex2jar

#convert dex to jar:
cd dex2jar
chmod +x *.sh
sh d2j-dex2jar.sh ../eitaa.apk 
#sh d2j-dex2jar.sh ../telegram.apk 

##3) decompile the jar files
# install jd-gui or jd-eclipse (http://jd.benow.ca/)
# e.g. in fedora
wget https://github.com/java-decompiler/jd-gui/releases/download/v1.4.0/jd-gui-1.4.0-0.noarch.rpm
sduo rpm -ivh jd-gui-1.4.0-0.noarch.rpm

##4) get the source code from jar file
#install jd-gui http://jd.benow.ca/
# run JD-GUI and browse jar files
# save the java source code

# unzip java source code
unzip -x eitaa-dex2jar.jar.src.zip -d eitaa-src
#unzip -x telegram-dex2jar.jar.src.zip -d telegram-src

##5) compare telegram-src eitaa-src
# use some tools like MOSS https://theory.stanford.edu/~aiken/moss/
# A very simple test
cd eitaa-src/ && grep -ri "telegram"


