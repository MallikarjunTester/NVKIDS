$Username = 'ProtechBN\duraipandi'
$Password = 'Sept2019'
$pass = ConvertTo-SecureString -AsPlainText $Password -Force
$Cred = New-Object System.Management.Automation.PSCredential -ArgumentList $Username,$pass
Invoke-Command -ComputerName "probs-178" -credential $cred -ErrorAction Stop -ScriptBlock {Invoke-Expression -Command:"cmd.exe /c 'c:\KILL BROWSER.bat'"}