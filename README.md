# Cloud Drive

<p>This website includes 3 main systems:  Storage system, User Registration, File System and File System. </p>

<p>The system allocated 30GB of storage to new user on the least used storage server;</p>
<p>Users can login with Google or Facebook accounts; view their profile; </p>

<p>Basic file operations include upload, download files and folders through drag and drop; copy cut or paste files and folders;delete and rename folders; display videos and pictures online; share files by public links. </p>
<br/>

Techniques
--------------
Backend: Java Server Faces framework on Glassfish, Frontend: PrimeFaces library, Database: MySql

Front-end and Android application communicate with backend through RESTful APIs. Model: MVC
<br/>

Architecture
------------
High availability for glassfish cluster, web clients and storage servers, iSCSI based storage
![arc]()
<br/>

Web APIS
------------
**User services**
<br/>
Register :  POST    /rest/auth/register
Login  :   POST /rest/auth/login
Logout  :  GET /rest/auth/logout/{username}
<br/>
<br/>

**File services**
getFileByName :   GET   rest/files/{username}/getFile
Upload File :  POST /rest/files/{username} /uploadTo/{folderId}
Download  :  GET  /rest/files/{username} /download/ {fileId}
addFolder : POST  /rest/files/{username}/addFolder
Rename  :  POST  /rest/files/{username}/rename
<br/>

Preview
------------


