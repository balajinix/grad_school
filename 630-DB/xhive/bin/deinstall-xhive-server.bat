@rem Unregister the xhive-server service, for when the deinstaller fails to do so
@rem Use this command if you want to deinstall the X-Hive/DB server.

net stop xhive-server
instsrv xhive-server remove

