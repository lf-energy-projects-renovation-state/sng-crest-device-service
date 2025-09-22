This implementation was created from https://schneide.blog/2022/06/27/running-a-containerized-activedirectory-for-developers/

LDAP Admin:
cn=Administrator,cn=Users,dc=gxf,dc=org
AdminS3cr3t

Set up users (passwords have to be entered manually)
samba-tool user create overdruk --given-name Over --surname Druk
samba-tool user create lampjeaan --given-name Lampje --surname Aan
samba-tool group create flex
samba-tool group create kod
samba-tool group addmembers kod overdruk
samba-tool group addmembers flex lampjeaan
