#!/bin/bash
#P1=$0
#P2=`dirname $0`
#P3=`pwd`
#echo ${P1}
#echo $P2
#echo $P3
cd /root
#PACK_NAME=idc_`date +%Y-%m-%d-%H-%M-%S`.tar.gz
#全量包
#tar -czvf ${PACK_NAME} -P server_new
#增量包
#tar -Pczf ${PACK_NAME} server_new/bin server_new/data
#mv ${PACK_NAME} /root/pack
#cd /root/pack

#全量包
function PackAll()
{
	PACK_ALL_NAME=idc_all_`date +%Y-%m-%d-%H-%M-%S`.tar.gz
	tar -Pczf ${PACK_ALL_NAME} server_new/bin server_new/config server_new/data server_new/lib server_new/logs server_new/reload.sh server_new/start.sh server_new/stop.sh server_new/sql
        mv ${PACK_ALL_NAME} /root/pack
        cd /root/pack

	echo -e "\033[0;34;1m增量包${PACK_ALL_NAME} md5 = `md5sum ${PACK_ALL_NAME} | awk '{print \$1}'`\033[0;34;0m"
        echo -ne "\033[0;34;1m请输入要发布的服务器: 99服：s9  | 退出请按任意键\033[0;34;0m"
        read SN
        if [ "$SN"X == "s1"X ]
        then
            scp ${PACK_ALL_NAME} shsj_dev@140.143.225.127:~/app
			scp /root/server_new/copyRegion.sh shsj_dev@140.143.225.127:~/app
            echo "======================================================="
            echo -e "\033[0;35;1m登录到140.143.225.127\033[0;35;0m"
            echo -e "\033[0;35;1m1. cd ~/app\033[0;35;0m"
            echo -e "\033[0;35;1m2. sh server_new/stop.sh\033[0;35;0m"
            echo -e "\033[0;35;1m3. tar -zxvf ${PACK_ALL_NAME}\033[0;35;0m"
            echo -e "\033[0;35;1m4. mv copyRegion.sh ./server_new && cd server_new && sh copyRegion.sh\033[0;35;0m"
            echo -e "\033[0;35;1m5. sh start.sh\033[0;35;0m"
	elif [ "$SN"X == "s2"X ]
	then
		scp ${PACK_ALL_NAME} shsj_dev@140.143.223.95:~/app
		scp /root/server_new/copyRegion.sh shsj_dev@140.143.223.95:~/app
        echo "======================================================="
        echo -e "\033[0;35;1m登录到140.143.223.95\033[0;35;0m"
        echo -e "\033[0;35;1m1. cd ~/app\033[0;35;0m"
        echo -e "\033[0;35;1m2. sh server_new/stop.sh\033[0;35;0m"
        echo -e "\033[0;35;1m3. tar -zxvf ${PACK_ALL_NAME}\033[0;35;0m"
		echo -e "\033[0;35;1m4. mv copyRegion.sh ./server_new && cd server_new && sh copyRegion.sh\033[0;35;0m"
        echo -e "\033[0;35;1m5. sh start.sh\033[0;35;0m"
	elif [[ "$SN"X == "s9"X ]]; then
		scp ${PACK_ALL_NAME} root@123.56.198.99:~/app
        #scp /root/server_new/copyRegion.sh 123.56.198.99:~/app
        echo "======================================================="
        echo -e "\033[0;35;1m登录到99服 ssh 123.56.198.99\033[0;35;0m"
        echo -e "\033[0;35;1m1. cd ~/app\033[0;35;0m"
        echo -e "\033[0;35;1m2. 蓝港测试1服：sh scp.sh ${PACK_ALL_NAME} s1 | 蓝港测试2服：sh scp.sh ${PACK_ALL_NAME} s2 | 蓝港IDC测试1服：sh scp.sh ${PACK_ALL_NAME} 81 | 蓝港IDC测试2服：sh scp.sh ${PACK_ALL_NAME} 82 | 海外正式服: sh scp.sh ${PACK_ALL_NAME} 83 | 英文版正式服: sh scp.sh ${PACK_ALL_NAME} 91 | 英文版备用服: sh scp.sh ${PACK_ALL_NAME} 92 | 英文版测试备用服: sh scp.sh ${PACK_ALL_NAME} 93 \033[0;35;0m"
    else
        echo -e  "\033[0;35;1mexit\033[0;35;0m"
        exit 0;
    fi
}



#增量包
function Pack()
{
	PACK_NAME=idc_`date +%Y-%m-%d-%H-%M-%S`.tar.gz
	tar -Pczf ${PACK_NAME} server_new/bin server_new/data
	mv ${PACK_NAME} /root/pack
	cd /root/pack
	
	echo -e "\033[0;34;1m增量包md5 = `md5sum ${PACK_NAME} | awk '{print \$1}'`\033[0;34;0m"
	#echo -e "\033[0;34;1m请输入要发布的服务器: 蓝港测试1服：s1 | 蓝港测试2服：s2 |  退出请按任意键\033[0;34;0m"
	echo -e "\033[0;34;1m请输入要发布的服务器: 99服：s9  | 云客服:ys |退出请按任意键\033[0;34;0m"
	read SN
	if [ "$SN"X == "s1"X ]
	then
		scp ${PACK_NAME} shsj_dev@140.143.225.127:~/app
		echo "======================================================="
		echo -e "\033[0;35;1m登录到140.143.225.127\033[0;35;0m"
		echo -e "\033[0;35;1m1. cd ~/app\033[0;35;0m"
		echo -e "\033[0;35;1m2. sh server_new/stop.sh\033[0;35;0m"
		echo -e "\033[0;35;1m3. tar -zxvf ${PACK_NAME}\033[0;35;0m"
		echo -e "\033[0;35;1m4. cd server_new && sh copyRegion.sh\033[0;35;0m"
		echo -e "\033[0;35;1m5. sh start.sh\033[0;35;0m"
	elif [ "$SN"X == "s2"X ]
	then
		scp ${PACK_NAME} shsj_dev@140.143.223.95:~/app
        echo "======================================================="
        echo -e "\033[0;35;1m登录到140.143.223.95\033[0;35;0m"
        echo -e "\033[0;35;1m1. cd ~/app\033[0;35;0m"
        echo -e "\033[0;35;1m2. sh server_new/stop.sh\033[0;35;0m"
        echo -e "\033[0;35;1m3. tar -zxvf ${PACK_ALL_NAME}\033[0;35;0m"
        echo -e "\033[0;35;1m4. cd server_new && sh copyRegion.sh\033[0;35;0m"
        echo -e "\033[0;35;1m5. sh start.sh\033[0;35;0m"
	elif [[ "$SN"X == "s9"X ]]; then
 		scp ${PACK_NAME} root@123.56.198.99:~/app
        #scp /root/server_new/copyRegion.sh 123.56.198.99:~/app
        echo "======================================================="
        echo -e "\033[0;35;1m登录到99服 ssh 123.56.198.99\033[0;35;0m"
        echo -e "\033[0;35;1m1. cd ~/app\033[0;35;0m"
        echo -e "\033[0;35;1m2. 蓝港测试1服：sh scp.sh ${PACK_NAME} s1 | 蓝港测试2服：sh scp.sh ${PACK_NAME} s2 | 蓝港IDC测试1服：sh scp.sh ${PACK_NAME} 81 | 蓝港IDC测试2服：sh scp.sh ${PACK_NAME} 82 | 海外正式服: sh scp.sh ${PACK_NAME} 83| 英文版正式服: sh scp.sh ${PACK_NAME} 91 | 英文版备用服: sh scp.sh ${PACK_NAME} 92 | 英文版测试备用服: sh scp.sh ${PACK_NAME} 93\033[0;35;0m"
	elif [[ "$SN"X == "ys"X ]]; then
		echo "======================================================="
		echo -e "\033[0;35;1m2. 云客测试1服：sh scp.sh ${PACK_NAME} s1 | 云客测试2服：sh scp.sh ${PACK_NAME} s2 | 云客测试3服：sh scp.sh ${PACK_NAME} s3 | 云客测试0服：sh scp.sh ${PACK_NAME} s4\033[0;35;0m"
	else
		echo -e  "\033[0;35;1mexit\033[0;35;0m"
		exit 0;
	fi
	
}

function ReadMe()
{
	echo -e "\033[0;34;1m开始打包\033[0;34;0m"
        echo -e "\033[0;34;1m全量打包请按: 1 | 增量打包请按: 2 | 其他请按任意键\033[0;34;0m"
	read PK
	case "$PK"X in
	    "1X")
		PackAll
		;;
	    "2X")
		Pack
		;;
	       *)
		ReadMe
		;;
	esac	
}

ReadMe
