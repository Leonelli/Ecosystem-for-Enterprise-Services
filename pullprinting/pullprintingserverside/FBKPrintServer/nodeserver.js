const express = require('express')
//var CloudPrint = require('node-gcp');
var CloudPrint = require('./myGCP');
var promiseRequest = require('request-promise');
var multer  = require('multer')
var upload = multer({ dest: 'uploads/' })
var download = multer({ dest: 'download/' })
var fs = require('fs');
var request = require('request');
var logGenerator = require('/home/pullprinting/FBK Print API Nodejs NOReSFRESH/logGenerator.js');

const app = express();
const port = 8080;

//CREATE A FUNCITION --> IsAnFbkAccount(accesstoken)
app.listen(port, () => console.log("Listening on port: ", port));


var _printerIDSpooler="4f6b7646-f5de-6950-684d-e393434aff2e";
var _printerMoveToExample="ebe62373-81ac-84db-be72-7806388dc21c" //to change
//var adminAccessToken = "ya29.GlsABx4kSxv0i5xPgoqZWhXAITKelbxaABBe2FX1rG-tM6PohW5kM83vHk9-p0d7mTQmiiWi42VgpcCuK6XkAizq6hFlH8wbmbxtJ-Hg5xRUoqV8aTrpVyvkPZ3O";
var adminAccessToken = "ya29.GlsBB7Kb4gwMNPow_EtVexpSRMaviWu1hwDy34uQ-pn34DRt9BQN1S-FPxt5-KeNznZ2NaDHNc3BcWNpdk5Vli8RjFlqkMEfAlpcYn9l3NeZPM1fEglFI1RLo00y";



//var adminRefreshToken = "1/YWahlQFdT2IA3L75g7GkMljU_T5fDoyKfuJa4LPlvW0";
var adminRefreshToken = "1/Ev7Jv5w3xOPdy67n7NIwMvVo7im-kx2CuBnj1P0Q2JnyT1kF4tquS4-itHrQWaeF";

app.get('/', (req, res) => {
    let code = req.query.code;
    let p = promiseRequest({
        method: 'POST',
        url:'https://www.googleapis.com/oauth2/v3/token',
        form: {
            code: code,
            redirect_uri: "http://127.0.0.1:8080",
            client_id: "306547292588-9nsde993lvbicbov5tt6olpeu5hbm5jv.apps.googleusercontent.com",
            client_secret: "ODzxsWa02b3pMeDAq5LZvAg_",
            grant_type: 'authorization_code'
        }
    });
    p.then((data) => res.send(data));
})

app.get('/list', (req, res) => {
    let accessToken = req.query.access_token;
    //let refreshToken = req.query.refresh_token;

    console.log("-------------LOOK----------------");
    console.log(accessToken);

    var printClient = new CloudPrint({
        clientId: "306547292588-9nsde993lvbicbov5tt6olpeu5hbm5jv.apps.googleusercontent.com",
        clientSecret: "ODzxsWa02b3pMeDAq5LZvAg_",
        accessToken: accessToken
        //refreshToken: refreshToken
    });
    
    console.log("-------check valid_user---------");
    
    printClient.getPrinters()
        .then(function(printers){
             res.send(printers);
             console.log(printers);
        });

});


app.get('/list_printer', (req, res) => {
    let accessToken = req.query.access_token;
    //let refreshToken = req.query.refresh_token;

    console.log("-------------LOOK----------------");
    console.log(accessToken);

    var printClientUser = new CloudPrint({
        clientId: "306547292588-9nsde993lvbicbov5tt6olpeu5hbm5jv.apps.googleusercontent.com",
        clientSecret: "ODzxsWa02b3pMeDAq5LZvAg_",
        accessToken: accessToken
        //refreshToken: refreshToken
    });

    var printClientAdmin = new CloudPrint({
        clientId: "306547292588-9nsde993lvbicbov5tt6olpeu5hbm5jv.apps.googleusercontent.com",
        clientSecret: "ODzxsWa02b3pMeDAq5LZvAg_",
        accessToken: adminAccessToken,
        refreshToken: adminRefreshToken
    });
    
    console.log("-------check valid_user---------");
    
    //if printClientUser is right user admin token for showing printer

    printClientUser.getMail()
    .then(function(domain){
        //res.send(domain);
        console.log(domain);
        if (!isValidDomain(domain)){
            res.status(401);
            res.send('User not Authorized, use an FBK account');
        }else{
            console.log("La MAIL ?? FBK.EU");
            console.log("-------LIST PRINTER---------");
            printClientAdmin.getPrinters()
                .then(function(printers){
                    res.send(printers);
                    console.log(printers);
                });
        }
    });
});


app.get('/queued', (req, res) => {
    let accessToken = req.query.access_token;
    //let refreshToken = req.query.refresh_token;



    var printClient = new CloudPrint({
        clientId: "306547292588-9nsde993lvbicbov5tt6olpeu5hbm5jv.apps.googleusercontent.com",
        clientSecret: "ODzxsWa02b3pMeDAq5LZvAg_",
        accessToken: accessToken,
        //refreshToken: refreshToken
    });
    
    printClient.getQueuedJobs()
        .then(function(jobs){
            console.log(jobs);
            res.send(jobs);
        });

});


function getFilesizeInBytes(filename) {
    const stats = fs.statSync(filename)
    const fileSizeInBytes = stats.size
    return fileSizeInBytes
}

app.post('/submit',upload.single('file'), (req, res) => {

    let accessToken = req.body.access_token;
    //let refreshToken = req.query.refresh_token;
    //console.log(req);
    let filePath = req.file.path;
    let contentType = req.body.contentType;
    let title = req.body.title;

    /*console.log(accessToken);
    console.log(refreshToken);
    console.log(contentType);
    console.log(title);
    console.log(_printerIDSpooler);*/

    var printClient = new CloudPrint({
        clientId: "306547292588-9nsde993lvbicbov5tt6olpeu5hbm5jv.apps.googleusercontent.com",
        clientSecret: "ODzxsWa02b3pMeDAq5LZvAg_",
        accessToken: accessToken,
        //refreshToken: refreshToken
    });
    
    fs.readFile(filePath, (err, content) => {
        //console.log("file", content);
        let p = printClient.print(_printerMoveToExample,content,contentType,title);
        p.then(data => {
            console.log(data);
            var jobID = data.job.id;
            res.send({'STATUS': 'SUBMITTED', 'ID': jobID});
        });
    });
});

app.post('/print',download.single('file'),(req, res) => {

    let accessToken = req.body.access_token; //inserire access token admin!
    //let refreshToken = req.query.refresh_token;
    let JobID = req.body.JobID;

    console.log(accessToken);
    //console.log(refreshToken);
    console.log(JobID);

    //lettura dei job e find del file corrispondente a JobID
     var printClient = new CloudPrint({
            clientId: "306547292588-9nsde993lvbicbov5tt6olpeu5hbm5jv.apps.googleusercontent.com",
            clientSecret: "ODzxsWa02b3pMeDAq5LZvAg_",
            accessToken: accessToken
            //refreshToken: refreshToken
    });
    

    //download file
    let p = printClient.download(JobID);
    p.then(data => {
        let path = "./download/"+JobID+".pdf";
        //console.log(data);
         fs.writeFile(path, data, 'binary', (err) => {
            if(err) {
                console.log(err);
            }
            else{
                console.log("The file was saved!"); 
                console.log(path);
            }
            console.log("--------------------------------------------------");
            //stampa su stampante corretta
            fs.readFile("./download/"+JobID+".pdf", (err, content) => {
                //console.log("file", content);
                let p2 = printClient.print(_printerMoveToExample,content,"application/pdf","docMossoCorrettamente");
                p2.then(data => {
                    console.log("data", data);
                    console.log("Print job nella stampante " + _printerMoveToExample);
                    //res.send({'STATUS': 'SUBMITTED'});
                    console.log("--------------------------------------------------");
                    //deleteJob
                    let p3 = printClient.deleteJob(JobID);
                    p3.then(data => {
                        //console.log(data);
                        console.log(data);
                        console.log("job eliminato "+ JobID);
                        res.send({'STATUS': 'SUBMITTED'});
                        //res.send({'STATUS': 'DELETED'});
                    });
                });
            });
           
        }); 
    });
    
});



app.post('/print_token',download.single('file'),(req, res) => {

    let accessToken = req.body.access_token; //inserire access token admin!
    //let refreshToken = req.query.refresh_token;
    let JobID = req.body.JobID;
    let printer_id_to_move = req.body.printerDestinationId;

    console.log(printer_id_to_move);

    console.log(accessToken);
    //console.log(refreshToken);
    console.log(JobID);

    console.log(adminRefreshToken);

    //lettura dei job e find del file corrispondente a JobID
     var printClientUser = new CloudPrint({
            clientId: "306547292588-9nsde993lvbicbov5tt6olpeu5hbm5jv.apps.googleusercontent.com",
            clientSecret: "ODzxsWa02b3pMeDAq5LZvAg_",
            accessToken: accessToken,
            refreshToken: "" //CHANGE THIS!!
    });

    var printClientAdmin = new CloudPrint({
        clientId: "306547292588-9nsde993lvbicbov5tt6olpeu5hbm5jv.apps.googleusercontent.com",
        clientSecret: "ODzxsWa02b3pMeDAq5LZvAg_",
        //oauth exchange code for get the admin token...same for printer List!
        accessToken: adminAccessToken,
        refreshToken: adminRefreshToken //usare quello dell'utente
    }); 
    
    //download file


    printClientUser.getMail()
    .then(function(domain){
        //res.send(domain);
        console.log(domain);
        if (domain !="fbk.eu"){
            res.status(401);
            res.send('User not Authorized, use an FBK account');
            console.log("User not Authorized, use an FBK account");
        }
        else{
            let p = printClientUser.download(JobID);
            p.then(data => {
                console.log("copiojob");
                let path = "./download/"+JobID+".pdf";
                //console.log(data);
                fs.writeFile(path, data, 'binary', (err) => {
                    if(err) {
                        console.log(err);
                    }
                    else{
                        console.log("The file was saved!"); 
                        console.log(path);
                    }
                    console.log("--------------------------------------------------");
                    //stampa su stampante corretta
                    fs.readFile("./download/"+JobID+".pdf", (err, content) => {
                        //console.log("file", content);
                        let p2 = printClientAdmin.print(printer_id_to_move,content,"application/pdf","docMossoCorrettamente");//qui admin
                        p2.then(data => {
                            console.log("data", data);
                            console.log("Print job nella stampante " + printer_id_to_move);
                            //res.send({'STATUS': 'SUBMITTED'});
                            console.log("--------------------------------------------------");
                            //deleteJob
                            let p3 = printClientUser.deleteJob(JobID);
                            p3.then(data => {
                                console.log(data);
                                console.log("job eliminato "+ JobID);
                                fs.unlink("./download/"+JobID+".pdf", function (err) {
                                    if (err) throw err;
                                     //if no error, file has been deleted successfully
                                    console.log('File deleted!');
                               }); 
                                res.send({'STATUS': 'SUBMITTED'});
                                //res.send({'STATUS': 'DELETED'});
                            });
                        });
                    });
                
                }); 
            });
        }
});
});


app.post('/print_token_old',download.single('file'),(req, res) => {

    let accessToken = req.body.access_token; //inserire access token admin!
    //let refreshToken = req.query.refresh_token;
    let JobID = req.body.JobID;
    let printer_id_to_move = req.body.printerDestinationId;

    console.log(printer_id_to_move);

    console.log(accessToken);
    //console.log(refreshToken);
    console.log(JobID);

    console.log(adminRefreshToken);

    //lettura dei job e find del file corrispondente a JobID
     var printClientUser = new CloudPrint({
            clientId: "306547292588-9nsde993lvbicbov5tt6olpeu5hbm5jv.apps.googleusercontent.com",
            clientSecret: "ODzxsWa02b3pMeDAq5LZvAg_",
            accessToken: accessToken,
            refreshToken: "" //CHANGE THIS!!
    });

    var printClientAdmin = new CloudPrint({
        clientId: "306547292588-9nsde993lvbicbov5tt6olpeu5hbm5jv.apps.googleusercontent.com",
        clientSecret: "ODzxsWa02b3pMeDAq5LZvAg_",
        //oauth exchange code for get the admin token...same for printer List!
        accessToken: adminAccessToken,
        refreshToken: adminRefreshToken //usare quello dell'utente
    }); 
    
    //download file


    printClientUser.getMail()
    .then(function(domain){
        //res.send(domain);
        console.log(domain);
        if (domain !="fbk.eu"){
            res.status(401);
            res.send('User not Authorized, use an FBK account');
            console.log("User not Authorized, use an FBK account");
        }
        else{
            let p = printClientUser.download(JobID);
            p.then(data => {
                console.log("copiojob");
                let path = "./download/"+JobID+".pdf";
                //console.log(data);
                fs.writeFile(path, data, 'binary', (err) => {
                    if(err) {
                        console.log(err);
                    }
                    else{
                        console.log("The file was saved!"); 
                        console.log(path);
                    }
                    console.log("--------------------------------------------------");
                    //stampa su stampante corretta
                    fs.readFile("./download/"+JobID+".pdf", (err, content) => {
                        //console.log("file", content);
                        let p2 = printClientAdmin.print(printer_id_to_move,content,"application/pdf","docMossoCorrettamente");//qui admin
                        p2.then(data => {
                            console.log("data", data);
                            console.log("Print job nella stampante " + printer_id_to_move);
                            //res.send({'STATUS': 'SUBMITTED'});
                            console.log("--------------------------------------------------");
                            //deleteJob
                           // let p3 = printClientUser.deleteJob(JobID);
                           // p3.then(data => {
                                //console.log(data);
                             //   console.log("job eliminato "+ JobID);
                              //  fs.unlink("./download/"+JobID+".pdf", function (err) {
                               //     if (err) throw err;
                                    // if no error, file has been deleted successfully
                                //    console.log('File deleted!');
                               // }); 
                                res.send({'STATUS': 'SUBMITTED'});
                                //res.send({'STATUS': 'DELETED'});
                           // });
                        });
                    });
                
                }); 
            });
        }
});
});

function deleteDownload(file_name)
{
        $.ajax({
          data: {'file' : "<?php echo dirname(__FILE__) . '/uploads/'?>" + file_name },
          success: function (response) {
             // do something
          },
          error: function () {
             // do something
          }
        });
}


app.get('/get_mail', (req, res) => {
    let accessToken = req.query.access_token;
    console.log(accessToken);

    var printClient = new CloudPrint({
        clientId: "306547292588-9nsde993lvbicbov5tt6olpeu5hbm5jv.apps.googleusercontent.com",
        clientSecret: "ODzxsWa02b3pMeDAq5LZvAg_",
        accessToken: accessToken
    });
    
    console.log("-------check MAIL form user---------");
    
    printClient.getMail()
    .then(function(domain){
        res.send(domain);
        console.log(domain);
   });
});






app.get('/get_mail_SC', (req, res) => {
    let accessToken = req.query.access_token;
    let sc_token = req.query.token;
    console.log("accessToken:"+accessToken);
    console.log("sc_token:"+sc_token)

    var printClient = new CloudPrint({
        clientId: "306547292588-9nsde993lvbicbov5tt6olpeu5hbm5jv.apps.googleusercontent.com",
        clientSecret: "ODzxsWa02b3pMeDAq5LZvAg_",
        accessToken: accessToken
    });
    
    console.log("-------check MAIL form user SMART COMMUNITY---------");
    
    printClient.getMail_SC(accessToken,sc_token)
    .then(function(domain){
        res.send(domain);
        console.log(domain);
   });
});







//test print with SC check mail

app.post('/print_token_SC',download.single('file'),(req, res) => {

    let accessToken = req.body.access_token; //inserire access token admin!
    //let refreshToken = req.query.refresh_token;
    let JobID = req.body.JobID;
    let printer_id_to_move = req.body.printerDestinationId;
    let sc_token = req.body.token;

    console.log("accessToken: "+accessToken);
    console.log("JobID: "+JobID);
    console.log("printer_id_to_move: "+printer_id_to_move);
    console.log("sc_token: "+sc_token);


    console.log(accessToken);
    //console.log(refreshToken);
    console.log(JobID);

    console.log(adminRefreshToken);

    //lettura dei job e find del file corrispondente a JobID
     var printClientUser = new CloudPrint({
            clientId: "306547292588-9nsde993lvbicbov5tt6olpeu5hbm5jv.apps.googleusercontent.com",
            clientSecret: "ODzxsWa02b3pMeDAq5LZvAg_",
            accessToken: accessToken,
            refreshToken: "" //CHANGE THIS!!
    });

    var printClientAdmin = new CloudPrint({
        clientId: "306547292588-9nsde993lvbicbov5tt6olpeu5hbm5jv.apps.googleusercontent.com",
        clientSecret: "ODzxsWa02b3pMeDAq5LZvAg_",
        //oauth exchange code for get the admin token...same for printer List!
        accessToken: adminAccessToken,
        refreshToken: adminRefreshToken //usare quello dell'utente
    }); 

    var printClientSC = new CloudPrint({
        clientId: "306547292588-9nsde993lvbicbov5tt6olpeu5hbm5jv.apps.googleusercontent.com",
        clientSecret: "ODzxsWa02b3pMeDAq5LZvAg_",
        accessToken: accessToken
    });
    
    //download file


var mail_SC;
var mail_FBK;
console.log("getMail_SC");
    printClientSC.getMail_SC(accessToken,sc_token)
    .then(function(user){
        //res.send(domain);
        mail_SC=user;
	console.log(user);

console.log("getMail_FBK");
    printClientSC.getMail_FBK()
    .then(function(user){
        //res.send(domain);
	mail_FBK=user;
        console.log(user);


console.log("getMail");
    printClientUser.getMail()
    .then(function(domain){
        //res.send(domain);
        console.log(domain);
	console.log("FBK:"+mail_FBK+" ,SC:"+mail_SC);
        if (!isValidDomain(domain) || mail_FBK!=mail_SC){
            res.status(401);
            res.send('User not Authorized, use an FBK account or CIE is not the correct one');
            console.log("User not Authorized, use an FBK account or CIE is not the correct one");
        }
        else{
	   console.log("download job?");
            let p = printClientUser.download(JobID);
            p.then(data => {
                console.log("copiojob");
                let path = "./download/"+JobID+".pdf";
                //console.log(data);
                fs.writeFile(path, data, 'binary', (err) => {
                    if(err) {
                        console.log(err);
                    }
                    else{
                        console.log("The file was saved!"); 
                        console.log(path);
                    }
                    console.log("--------------------------------------------------");
                    //stampa su stampante corretta
                    fs.readFile("./download/"+JobID+".pdf", (err, content) => {
                        //console.log("file", content);
                        let p2 = printClientAdmin.print(printer_id_to_move,content,"application/pdf","docMossoCorrettamente");//qui admin
                        p2.then(data => {
                            console.log("data", data);
                            console.log("data", data.success);
                            if(data.success==false){
                                res.status(401);
                                res.send( data.message);
                                console.log("datamessage:"+data.message);
                       		 }
                            else{
			    console.log("Print job nella stampante " + printer_id_to_move);
                            //res.send({'STATUS': 'SUBMITTED'});
                            console.log("--------------------------------------------------");
			    let p4 = printClientUser.jobLookup(JobID);
                            p4.then(data => {
                                ownerid = data.job.ownerId;
                                title = data.job.title;
                                numberOfPages=data.job.numberOfPages;
			    //deleteJob
                            let p3 = printClientUser.deleteJob(JobID);
                            p3.then(data => {
                                //console.log(data);
                                console.log("job eliminato "+ JobID);
                                fs.unlink("./download/"+JobID+".pdf", function (err) {
                                    if (err) throw err;
                                    // if no error, file has been deleted successfully
                                    console.log('File deleted!');
                                });
                                logGenerator.salvaLog(ownerid,title,numberOfPages,printer_id_to_move,"SI");
                                res.send({'STATUS': 'SUBMITTED'});
                                //res.send({'STATUS': 'DELETED'});
                            });
                        });
			}
 			});
                    });
                
                }); 
            });
        }
});
});
});
});




//test EASY print 

app.post('/print_token_easy',download.single('file'),(req, res) => {

    let accessToken = req.body.access_token;
 //inserire access token admin!
    //let refreshToken = req.query.refresh_token;
    let JobID = req.body.JobID;
    let printer_id_to_move = req.body.printerDestinationId;

    console.log(printer_id_to_move);

    console.log(accessToken);
    //console.log(refreshToken);
    console.log(JobID);

    console.log(adminRefreshToken);

    //lettura dei job e find del file corrispondente a JobID
     var printClientUser = new CloudPrint({
            clientId: "306547292588-9nsde993lvbicbov5tt6olpeu5hbm5jv.apps.googleusercontent.com",
            clientSecret: "ODzxsWa02b3pMeDAq5LZvAg_",
            accessToken: accessToken,
            refreshToken: "" //CHANGE THIS!!
    });

    var printClientAdmin = new CloudPrint({
        clientId: "306547292588-9nsde993lvbicbov5tt6olpeu5hbm5jv.apps.googleusercontent.com",
        clientSecret: "ODzxsWa02b3pMeDAq5LZvAg_",
        //oauth exchange code for get the admin token...same for printer List!
        accessToken: adminAccessToken,
        refreshToken: adminRefreshToken //usare quello dell'utente
    }); 
    
    //download file

//var fs = require('fs');
var mail_FBK;
console.log("getMail_FBK");
    printClientUser.getMail_FBK()
    .then(function(user){
        //res.send(domain);
	mail_FBK=user;
        console.log(user);

console.log("getMail");
    printClientUser.getMail()
    .then(function(domain){
        //res.send(domain);
        console.log(domain);
	console.log("FBK:"+mail_FBK);
	console.log(isValidDomain(domain));
        if (!isValidDomain(domain)){
            res.status(401);
            res.send('User not Authorized, use an FBK account');
            console.log("User not Authorized, use an FBK account");
        }
        else{
            let p = printClientUser.download(JobID);
            p.then(data => {
                console.log("copiojob");
                let path = "./download/"+JobID+".pdf";
                //console.log(data);
                fs.writeFile(path, data, 'binary', (err) => {
                    if(err) {
                        console.log(err);
                    }
                    else{
                        console.log("The file was saved!"); 
                        console.log(path);
                    }
                    console.log("--------------------------------------------------");
                    //stampa su stampante corretta
                    fs.readFile("./download/"+JobID+".pdf", (err, content) => {
                        //console.log("file", content);
                        let p2 = printClientAdmin.print(printer_id_to_move,content,"application/pdf","docMossoCorrettamente");//qui admin
                        p2.then(data => {
                            console.log(data);
                            console.log("data", data.success);
                            if(data.success==false){
                                res.status(401);
                                res.send( data.message);
                                console.log("datamessage:"+data.message);
                        	}
                       		else{
				console.log("Print job nella stampante " + printer_id_to_move);
			    //res.send({'STATUS': 'SUBMITTED'});
                            console.log("--------------------------------------------------");
                            //deleteJob
                            let p4 = printClientUser.jobLookup(JobID);
                            p4.then(data => {
                                ownerid = data.job.ownerId;
                                title = data.job.title;
                                numberOfPages=data.job.numberOfPages;
                            let p3 = printClientUser.deleteJob(JobID);
                            p3.then(data => {
                                //console.log(data);
                                console.log("job eliminato "+ JobID);
                                fs.unlink("./download/"+JobID+".pdf", function (err) {
                                    if (err) throw err;
                                    // if no error, file has been deleted successfully
                                    console.log('File deleted!');
                                });
                                logGenerator.salvaLog(ownerid,title,numberOfPages,printer_id_to_move,"NO");
                                //fs.close();
				res.send({'STATUS': 'SUBMITTED'});
                                //res.send({'STATUS': 'DELETED'});
                            });
                        });
			}
                        });
                    });
                });
            });
        }
});
});
});





app.post('/deletejob', (req, res) => {
    let accessToken = req.query.access_token;
    let JobID = req.query.jobid;

    console.log("-------------LOOK----------------");
    console.log(accessToken);
    console.log(JobID);

    var printClient = new CloudPrint({
        clientId: "306547292588-9nsde993lvbicbov5tt6olpeu5hbm5jv.apps.googleusercontent.com",
        clientSecret: "ODzxsWa02b3pMeDAq5LZvAg_",
        accessToken: accessToken
    });
    console.log("-------check valid_user---------"); 
    printClient.deleteJob(JobID)
        .then(function(status){
             res.send(status);
             console.log(status);
        });

});


function isValidDomain(domain){
    var __dirname = "/home/pullprinting/"
    var fs = require('fs'),
    contents = fs.readFileSync(__dirname+'domain.txt', 'utf8');
    data = contents.split("\n");
    valid_domain = data.includes(domain);
    console.log("controllo dominio..."+  valid_domain);
    return valid_domain;
}

app.get('/queueJob',(req, res) => {
console.log("queueJob");
var options = { method: 'POST',
  url: 'https://www.google.com/cloudprint/submit',
  qs: { printerid: 'f956b231-f4d7-fb5c-5c77-595fd684ac8e' },
  headers: 
   { 'cache-control': 'no-cache',
     Connection: 'keep-alive',
     'Content-Length': '4461',
     Cookie: 'NID=181=EB5Gjjad-s7uGT4WALS87FJolkxgEm_zhAhfRnaXrZ2bXP8uP7j49quXsV-RilrpGapJLBFjlNdmpx6jByFcxoR6r_QiNkJPPQDd6xgMHWLOtEEtKCOyZ4dAKu26JYfD0LT1qCPLDOrYpY-cWMLis_nbkb5ZbEpsW4we5RIs-H51U0o7JRuKuLNU0oIYk0SwQZrZpuU',
     'Accept-Encoding': 'gzip, deflate',
     Host: 'www.google.com',
     'Postman-Token': 'd11d7810-ead2-46b8-aa91-3ece1c378d91,22cb6de8-d71e-4a31-9a63-dabecf08e3d2',
     'Cache-Control': 'no-cache',
     Accept: '*/*',
     'User-Agent': 'PostmanRuntime/7.17.1',
     'Content-Type': 'application/x-www-form-urlencoded',
     Authorization: 'OAuth   ya29.Il-PB5OsI_E18HU5I9nVciiXh0N4EH8TWWl9bAuDdCrVm_GsIis9x9l1viMcA84kJ3tW3RIkgLbJKGTN66Sft8OIEtVYIwgLYY4Fqm1igagPGg6OW_qaxxp4Eh8y261l1w',
     'X-CloudPrint-Proxy': 'node-gcp',
     'content-type': 'multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW' },
  formData: 
   { content: 
      { value: 'fs.createReadStream("test.pdf")',
        options: { filename: 'test.pdf', contentType: null } },
     contentType: 'application/pdf',
     title: 'test.pdf' } };

request(options, function (error, response, body) {
  if (error) throw new Error(error);

  console.log(body);
});

})


app.post('/newJobs',upload.single('file'), (req, res) => {

    console.log(req.body.nFile);
    for (i = 0; i < req.body.nFile; i++) { 

    let accessToken = req.body.access_token;
    let filePath = req.file.path; 
    console.log("filePath: "+filePath);
    let contentType = req.body.contentType;
    let title = req.body.title;

    /*console.log(accessToken);
    console.log(refreshToken);
    console.log(contentType);
    console.log(title);
    console.log(_printerIDSpooler);*/

    var printClient = new CloudPrint({
        clientId: "306547292588-9nsde993lvbicbov5tt6olpeu5hbm5jv.apps.googleusercontent.com",
        clientSecret: "ODzxsWa02b3pMeDAq5LZvAg_",
        accessToken: accessToken,
        //refreshToken: refreshToken
    }); 
    fs.readFile(filePath, (err, content) => {
        //console.log("file", content);
        let p = printClient.print("f956b231-f4d7-fb5c-5c77-595fd684ac8e",content,contentType,title);
        p.then(data => {
            console.log(data);
            var jobID = data.job.id;
            res.send({'STATUS': 'SUBMITTED', 'ID': jobID});
        });
    });
}
});
