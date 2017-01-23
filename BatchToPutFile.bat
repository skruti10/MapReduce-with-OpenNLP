ECHO OFF

cd/

cd C:\ksharm22hw2\ResPaperDownloadParse\src\main\resources

cd %1

for %%f in (*.*) do (

echo %%f

cd C:\ksharm22hw2

PSCPCopyFilesRoot C:\ksharm22hw2\ResPaperDownloadParse\src\main\resources\%1\%%f

cd C:\ksharm22hw2\ResPaperDownloadParse\src\main\resources

cd %1


)

cd/

cd C:\ksharm22hw2

TestBatch

PAUSE