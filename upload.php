 <?php
 if($_SERVER['REQUEST_METHOD']=='POST')
 {
         $target_dir = "uploads/";
         $path = $_FILES['fileToUpload']['name'];
         $ext = pathinfo($path, PATHINFO_EXTENSION);
         $x=rand(100000,10000000);
         $y=rand(100000,10000000);
         $new_name=$x."_".$y.".".$ext;
         $target_file = $target_dir .$new_name;
         if (move_uploaded_file($_FILES["fileToUpload"]["tmp_name"], $target_file))
         {
           echo "Successfully Uploaded";
         }else
         {
           echo "Sorry, there was an error uploading your file.";
         }        
 }
 else
 {
   echo "Error";
 }