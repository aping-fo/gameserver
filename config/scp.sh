#!/bin/bash
if [ $# -eq 0 ]
then
        echo "请输入需要拷贝的文件"
        exit 0
fi
pack=$1
SN=$2
if [[ "$SN"X == "s1"X ]]; then
		scp -i /root/.ssh/my_world_ras -P23232 /root/pack/${pack} root@120.92.153.215:~/app
        #scp ${pack} shsj_dev@140.143.239.191:~/app
        #scp copyRegion.sh shsj_dev@140.143.239.191:~/app
        echo "=======================云客测试1区================================"
        echo -e "\033[0;35;1m登录到  ssh -i /root/.ssh/my_world_ras root@120.92.153.215 -p 23232\033[0;35;0m"
        echo -e "\033[0;35;1m1. cd ~/app/\033[0;35;0m"
        echo -e "\033[0;35;1m2. sh server_new/stop.sh\033[0;35;0m"
        echo -e "\033[0;35;1m3. tar -zxvf ${pack}\033[0;35;0m"
        echo -e "\033[0;35;1m6. sh start.sh\033[0;35;0m"
elif [[ "$SN"X == "s2"X ]]; then
		scp -i /root/.ssh/my_world_ras -P23232 /root/pack/${pack} root@120.92.139.89:~/app
        #scp ${pack} shsj_dev@140.143.239.191:~/app/server2
        echo "=======================蓝港精英测试1区================================"
		echo -e "\033[0;35;1m登录到  ssh -i /root/.ssh/my_world_ras root@120.92.139.89:-p 23232\033[0;35;0m"
        echo -e "\033[0;35;1m1. cd ~/app/\033[0;35;0m"
        echo -e "\033[0;35;1m2. sh server_new/stop.sh\033[0;35;0m"
        echo -e "\033[0;35;1m3. tar -zxvf ${pack}\033[0;35;0m"
        echo -e "\033[0;35;1m6. sh start.sh\033[0;35;0m"
elif [[ "$SN"X == "s3"X ]]; then
		scp -i /root/.ssh/my_world_ras -P23232 /root/pack/${pack} root@120.92.143.237:~/app
        #scp ${pack} shsj_dev@140.143.225.127:~/app/
        echo "=======================蓝港精英测试1区================================"
		echo -e "\033[0;35;1m登录到  ssh -i /root/.ssh/my_world_ras root@120.92.143.237:-p 23232\033[0;35;0m"
        #echo -e "\033[0;35;1m登录到 ssh shsj_dev@140.143.225.127\033[0;35;0m" 
        echo -e "\033[0;35;1m1. cd ~/app/\033[0;35;0m"
        echo -e "\033[0;35;1m2. sh server_new/stop.sh\033[0;35;0m"
        echo -e "\033[0;35;1m3. tar -zxvf ${pack}\033[0;35;0m"
        echo -e "\033[0;35;1m6. sh start.sh\033[0;35;0m"
elif [[ "$SN"X == "s4"X ]]; then
        scp -i /root/.ssh/my_world_ras -P23232 /root/pack/${pack} root@120.92.159.151:~/app
        #scp ${pack} shsj_dev@120.92.159.151:~/app/
        echo "=======================蓝港精英测试1区================================"
        echo -e "\033[0;35;1m登录到  ssh -i /root/.ssh/my_world_ras root@120.92.159.151:-p 23232\033[0;35;0m"
        #echo -e "\033[0;35;1m登录到 ssh shsj_dev@140.143.225.127\033[0;35;0m"-
        echo -e "\033[0;35;1m1. cd ~/app/\033[0;35;0m"-
        echo -e "\033[0;35;1m2. sh server_new/stop.sh\033[0;35;0m"-
        echo -e "\033[0;35;1m3. tar -zxvf ${pack}\033[0;35;0m"-
        echo -e "\033[0;35;1m6. sh start.sh\033[0;35;0m"-
else
	exit 0;
fi
