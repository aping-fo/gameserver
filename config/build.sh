#!/bin/bash
#CUR_DIR=`pwd`
CUR_DIR=/var/lib/jenkins/workspace/app
cd ${CUR_DIR}
#echo ${CUR_DIR}
#echo "begin to del bin"
rm -rf bin/
rm -rf dist/
svn cleanup
#svn update –set-depth=exclude dist bin
#svn update

svn up data
svn up src
svn up lib

SVN_VERSION=`svn info|grep Revision|awk '{print$2}'`
LAST_VERSION=`cat svn.txt`
echo -e "\033[36;49;1mLAST VERSION IS ====> ${LAST_VERSION}\033[39;49;0m"
echo -e "\033[31;48;1mCURRENT VERSION IS ======> ${SVN_VERSION}\033[39;48;0m"
if [ "${SVN_VERSION}" -eq "${LAST_VERSION}" ]
then
        echo "Nothing to do."
	echo "nothing to do."
#        exit ${LAST_VERSION}
fi

/root/apache-ant-1.10.1/bin/ant -file ./build.xml
#ant
if [ $? -eq 0 ]
then
	echo "build finish............."
	#设置最后成功编译版本号
	echo "${SVN_VERSION}" > svn.txt
else
	echo "build fail........."
	exit -1
fi
chmod +x ./start.sh
chmod +x ./stop.sh
chmod +x ./restart.sh

#/bin/cp -rf ./start.sh ./dist/gameserver
#/bin/cp -rf ./stop.sh ./dist/gameserver
#/bin/cp -rf ./restart.sh ./dist/gameserver
#/bin/cp -rf ./dist/* /root/app/
/bin/cp -rf ./dist/gameserver/* /root/server_new/
if [ $? -eq 0 ] 
then
	echo "deploy success .................."
else
	echo "deploy fail,target dir dont exist ????? .................."
	exit -1
fi
#/bin/cp -rf ./svn.txt /root/.pack/
#mkdir -p /root/.pack/idc_v_`date "+%F"`_${SVN_VERSION}/app
#/bin/cp -prf ./dist/* /root/.pack/idc_v_`date "+%F"`_${SVN_VERSION}/app
#scp -r ./dist/gameserver/bin/* root@123.56.198.99:/home/game2/test2/bin/
#scp -r ./dist/gameserver/data/* root@123.56.198.99:/home/game2/test2/data/
#rm -rf bin/
#svn update
#svn cleanup
#svn add dist --force
#svn commit dist -m"for test"
#输入915，进行远程更新重启
if [ $# -gt 0 ] && [ $1 -eq 915 ]
then
	sh scp.sh
	ssh root@123.56.198.99 "sh /home/game2/test2/stop.sh"
	ssh root@123.56.198.99 "sh /home/game2/test2/start.sh"
fi

if [ $# -gt 0 ] && [ $1 -eq 529 ]
then
	echo "begin to tar ..."
	cd ./dist
	file="app_`date +%Y-%m-%d`_${SVN_VERSION}.tgz"
	tar -czvf ${file} ./gameserver/*
	cd ../
	echo "tar finish ..."
fi

#删除.svn目录
function delSvnDir(){
        for dir in `ls $1`
        do
                svnDir=$1"/"$dir
                if [ -d $svnDir ]
                then
                        svn_dir_name=`basename $svnDir`
                        if [ $svn_dir_name = ".svn" ]
                        then
				echo $svn_dir_name
                                echo $svnDir
                                rm -rf $svnDir
                        else
                                delSvnDir $svnDir
                        fi

                 fi
        done
}
delSvnDir "/var/lib/jenkins/workspace/app/dist/gameserver" 

cd ../dist
#svn cleanup
#svn update
#/bin/cp -rf ../app/dist/gameserver/* ./
#/bin/cp -rf ../app/config/* ./config/
#svn add * --force
#echo "commit begin."
#svn commit -m "commit server for test"
#echo "commit end."
