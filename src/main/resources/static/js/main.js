const socket = new WebSocket("ws://172.16.201.101:3001/web-socket");

socket.onopen = function (){
    console.log("connect to WebSocket");
};

socket.onmessage = function (data){
    let tallyData = JSON.parse(data.data);

    switch(tallyData.tallyState){
        case "PGM":
            changeState("program", tallyData);
            break;

        case "PRV":
            changeState("preview", tallyData);
            break;

        case "PGM_PRV":
            changeState("program", tallyData);
            changeState("preview", tallyData);
            break;
    }
};

socket.onclose = function (code){
    console.log("Close socket with code: " + code);
};

function changeState1(blocks, id, state){
    blocks.forEach(block => {
        let blockElem = document.getElementById(block + "_" + id);
        console.log(state)
        if (state === "EMPTY"){

        }else {
            blockElem.classList.add(block + "-color");
        }
    });
}

function changeState(type, tallyData){
    let inputBlocks = document.getElementById(type);
    let buttons = inputBlocks.getElementsByClassName("button-overlay");

    for (let button of buttons) {
        if (button.id === (type + "_" + tallyData.id)){
            button.classList.add(type + "-color");
        } else {
            button.classList.remove(type + "-color");
        }

    }
}