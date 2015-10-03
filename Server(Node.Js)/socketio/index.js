// Setup basic express server
var express = require('express');
var app = express();
var server = require('http').createServer(app);
var io = require('../..')(server);
var port = process.env.PORT || 3000;

//라우팅 시작시 몇번포트에서 서비스를 실행하는지 콘솔창에 출력
server.listen(port, function () {
  console.log('Server listening at port %d', port);
});

// Routing
app.use(express.static(__dirname + '/public'));

// Chatroom

// usernames which are currently connected to the chat
var usernames = {};
var rooms = ['standBy', 'room'];
var numUsers = 0;
var socket_ids = [];

function findClientsSocketByRoomId(roomId) {
	var res = [], room = io.sockets.adapter.rooms[roomId];
	if (room) {
		for (var id in room) {
			var socketData = io.sockets.adapter.nsp.connected[id];
			res.push(socketData.username);
		}
	}
	console.log(res);
	return res;
}

io.on('connection', function (socket) {
	var addedUser = false;
	console.log("SomeOne was Connted");

	socket.on('add user', function (username) {
		// we store the username in the socket session for this client
		socket.username = username;
		// add the client's username to the global list
		usernames[username] = username;
		++numUsers;
		addedUser = true;
		
			//대기룸에 포함시킨다.
		socket.join('standBy');
		socket.room="standBy";
			
		console.log(username + " : Joined : " + numUsers + " : to standBy Room");
		
		socket.emit('login', {
		  numUsers: numUsers
		});
	});
  
	socket.on('disconnection', function(){
		if (addedUser) {
			--numUsers;
			console.log(username + " : Left - " + numUsers + " from " + socket.room);
			
			socket.leave(socket.room);
			delete usernames[socket.username];
			
			io.sockets.emit('updateusers', usernames);
			socket.leave(socket.room);
		}
	});
	
	socket.on('mkRoom', function(){
		// 방 생성 + 입장
		// room 집합에 생성을 신청한 socket이름을 넣고 그 그룹에 삽입한다. 삽입한 후에는 소켓네임을 전달한다.
		
		rooms.push(socket.username);
		socket.join(socket.username);
		socket.room = socket.username;
		//registerUser(socket, socket.username);
		
		console.log("user : " + socket.username + " was make room and Enter : " + socket.room);
		
		socket.emit('mkRoom', {
		  roomID : socket.username,
		  member : findClientsSocketByRoomId(socket.username)
		});
	});
	
	socket.on('rmRoom', function(){
		// 방 제거
		/*socket.get('nickname',function(err,nickname){
            if(nickname != undefined){
                delete socket_ids[nickname];
                io.sockets.emit('userlist',{users:Object.keys(socket_ids)});
                                
            }// if
        });
		*/
		console.log("user : " + socket.username + " was remove room and Left");
		socket.broadcast.emit('exRoom', socket.username);
		socket.leave('/'+socket.room);
		
		socket.join("standBy");
		socket.room = "standBy";
	});
	
	socket.on('enRoom', function(roomNumber){
		// 방에 입장할 때
		console.log("user : " + socket.username + " was Enter room :: " + roomNumber);
		
		socket.join(roomNumber);
		socket.room = roomNumber;
		//registerUser(socket, socket.username);
		
		socket.broadcast.emit('user joined', {
		  username: socket.username,
		  numUsers: numUsers,
		  member : findClientsSocketByRoomId(roomNumber)
		});
		
		socket.emit('enterRoom', {
		  roomID : socket.room,
		  member : findClientsSocketByRoomId(roomNumber)
		});
		
		findClientsSocketByRoomId(roomNumber);
	});
	
	socket.on('exRoom', function(){
		// 방에서 퇴장할 때
		// 현재 룸을 떠난다(현재 접속한 룸은 세션에 저장되어 있다) 
		
		/*socket.get('nickname',function(err,nickname){
            if(nickname != undefined){
                delete socket_ids[nickname];
                io.sockets.emit('userlist',{users:Object.keys(socket_ids)});
            }// if
        });
		*/
		console.log("user : " + socket.username + " was left room :: " + socket.room);
		
		socket.leave(socket.room);
		
		socket.join("standBy");
		socket.room = "standBy";
		
		socket.broadcast.emit('exitRoom', {
		  username: socket.username,
		  numUsers: numUsers
		});
	});
	
	socket.on('sendMessage', function(data){
		if(socket.room != "standBy"){
			console.log("user : " + socket.username + " send data in room <" + socket.room +"> - " + data);
		
			socket.broadcast.in(socket.room).emit("message", {
				username: socket.username,
				message: data
			});
		}
		// 방 내부 메시지 전송
	});
});
/*
setInterval(function(){
  // console.log(io.of(defaultNsps).adapter.rooms[theRoom]);
  //console.log(io.nsps["/"].adapter.rooms["Qwe"].username);
	var roomMembers = 
	console.log(roomMembers);
}, 1000);
*/