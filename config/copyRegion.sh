#!/bin/bash
if [ $# -eq 0 ]
then
    echo -e "\033[0;34;1m配置拷贝 内网1服：1 | 蓝港测试：61 | 蓝港测试2服：62 | 蓝港外网测试服：81 | 下面的region为该行说明\033[0;34;0m"
    echo -e "\033[0;34;1msh copyRegion.sh region\033[0;34;0m"
    exit 0
fi


function ReadMe()
{
	echo -e "\033[0;34;1m配置拷贝\033[0;34;0m"
        echo -e "\033[0;34;1m\033[0;34;0m"
}

serverId=$1
case ${serverId} in
	1)
	  sed -i "s/192.168.7.200:3306\/game_new/192.168.0.200:3306\/game_new/g" ./config/application.xml
	  sed -i "s/127.0.0.1:3306\/game2_log/127.0.0.1:3306\/game_new_log/g" ./config/application.xml
	  sed -i "s/port=10010/port=10010/g" ./config/sys.properties
	  #sed -i "s/serverId=10/serverId=19/g" ./config/sys.properties
	  sed -i "s/gmport=20010/gmport=20011/g" ./config/sys.properties
	  sed -i "s/validate=false/validate=true/g" ./config/sys.properties
	  echo -e "\033[0;34;1m[${serverId}]服配置拷贝完毕\033[0;34;0m"
	;;

	61)
      sed -i "s/192.168.0.200:3306\/game_new/127.0.0.1:3306\/game_61/g" ./config/application.xml
      #sed -i "s/127.0.0.1:3306\/game2_log/127.0.0.1:3306\/game_61_log/g" ./config/application.xml
	  sed -i "s/127.0.0.1:3306\/game_new_log/127.0.0.1:3306\/game_61_log/g" ./config/application.xml
	  sed -i "s/127.0.0.1:3306\/game2_log/127.0.0.1:3306\/game_61_log/g" ./config/application.xml
      sed -i "s/name=\"password\" value=\"root\"/name=\"password\" value=\"BeGg3N2b@2f\"/g" ./config/application.xml
      sed -i "s/port=10010/port=10011/g" ./config/sys.properties
      sed -i "s/serverId=10/serverId=19/g" ./config/sys.properties
      #sed -i "s/gmport=20010/gmport=20011/g" ./config/sys.properties
	  sed -i "s/game_way_id = 213991/game_way_id = 213999/g" ./config/sys.properties
	  #sed -i "s/debug=true/debug=false/g" ./config/sys.properties
	  sed -i "s/validate=false/validate=true/g" ./config/sys.properties
	  echo -e "\033[0;34;1m${serverId}服配置拷贝完毕\033[0;34;0m"
	;;

	62)
      sed -i "s/192.168.0.200:3306\/game_new/127.0.0.1:3306\/game_62/g" ./config/application.xml
      sed -i "s/127.0.0.1:3306\/game_new_log/127.0.0.1:3306\/game_62_log/g" ./config/application.xml
	  sed -i "s/127.0.0.1:3306\/game2_log/127.0.0.1:3306\/game_62_log/g" ./config/application.xml
   	  sed -i "s/name=\"password\" value=\"root\"/name=\"password\" value=\"BeGg3N2b@2f\"/g" ./config/application.xml
      sed -i "s/port=10010/port=10011/g" ./config/sys.properties
      sed -i "s/serverId=10/serverId=20/g" ./config/sys.properties
      #sed -i "s/gmport=20010/gmport=20011/g" ./config/sys.properties
	  sed -i "s/debug=true/debug=false/g" ./config/sys.properties
	  sed -i "s/validate=false/validate=true/g" ./config/sys.properties
      sed -i "s/game_way_id = 213991/game_way_id = 213999/g" ./config/sys.properties
	  echo -e "\033[0;34;1m${serverId}服配置拷贝完毕\033[0;34;0m"
        ;;

	81)
          sed -i "s/192.168.0.200:3306\/game_new/127.0.0.1:3306\/game_81/g" ./config/application.xml
          sed -i "s/127.0.0.1:3306\/game_new_log/127.0.0.1:3306\/game_log_81/g" ./config/application.xml
	      sed -i "s/127.0.0.1:3306\/game2_log/127.0.0.1:3306\/game_log_81/g" ./config/application.xml
          sed -i "s/name=\"password\" value=\"root\"/name=\"password\" value=\"BeGg3N2b@2f\"/g" ./config/application.xml
          sed -i "s/port=10010/port=10011/g" ./config/sys.properties
          sed -i "s/serverId=10/serverId=20/g" ./config/sys.properties
          #sed -i "s/gmport=20010/gmport=20011/g" ./config/sys.properties
	  	  sed -i "s/debug=true/debug=false/g" ./config/sys.properties
	  	  sed -i "s/gm=true/gm=false/g" ./config/sys.properties
	  	  sed -i "s/game_way_id = 213999/game_way_id = 213993/g" ./config/sys.properties
		  sed -i "s/game_way_id = 213991/game_way_id = 213993/g" ./config/sys.properties
	  	  sed -i "s/startUpDate=2016-10-10 00:00:00/startUpDate=2018-3-21 11:00:00/g" ./config/sys.properties
	  	  sed -i "s/validate=false/validate=true/g" ./config/sys.properties
	  	echo -e "\033[0;34;1m${serverId}服配置拷贝完毕\033[0;34;0m"
        ;;
	82)
		  sed -i "s/192.168.0.200:3306\/game_new/127.0.0.1:3306\/game_82/g" ./config/application.xml
          sed -i "s/127.0.0.1:3306\/game_new_log/127.0.0.1:3306\/game_log_82/g" ./config/application.xml
          sed -i "s/127.0.0.1:3306\/game2_log/127.0.0.1:3306\/game_log_82/g" ./config/application.xml
          sed -i "s/name=\"password\" value=\"root\"/name=\"password\" value=\"BeGg3N2b@2f\"/g" ./config/application.xml
          sed -i "s/port=10010/port=10012/g" ./config/sys.properties
          sed -i "s/serverId=10/serverId=21/g" ./config/sys.properties
          sed -i "s/gmport=20010/gmport=20012/g" ./config/sys.properties
          sed -i "s/debug=true/debug=false/g" ./config/sys.properties
          sed -i "s/gm=true/gm=false/g" ./config/sys.properties
          sed -i "s/game_way_id = 213999/game_way_id = 213993/g" ./config/sys.properties
		  sed -i "s/game_way_id = 213991/game_way_id = 213993/g" ./config/sys.properties
          sed -i "s/sdk_port = 9801/sdk_port = 9802/g" ./config/sys.properties
          sed -i "s/startUpDate=2016-10-10 00:00:00/startUpDate=2018-3-23 11:00:00/g" ./config/sys.properties
		  sed -i "s/validate=false/validate=true/g" ./config/sys.properties
          echo -e "\033[0;34;1m${serverId}服配置拷贝完毕\033[0;34;0m"
        ;;
	*)
	  echo ""
	  exit
	  ;;
esac
