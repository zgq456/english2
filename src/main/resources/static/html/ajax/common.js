function updateRank(wordId, newRank) {
	$.ajax({
		type : "POST",
		url : "../../updateRank",
		data : {
			id : wordId,
			rank : newRank,
		},
		async : true,
		dataType : "json",
		success : function(data) {
			//rowData.tempRank = parseRank(newRank);
			//$("#grid-table").jqGrid('setRowData', id, rowData);
		},
		error : function(data) {
			alert("set error, please contact administrator");
		}

	});
}

function toggleSenMark(id, subgridTableId) {
//	alert("subgridTableId:" + subgridTableId);
	var rowData = jQuery("#" + subgridTableId).getRowData(id);
//	alert("rowData:" + rowData);
	$.ajax({
		type : "POST",
		url : "../../toggleMarkSen",
		data : {
			id : id
		},
		async : true,
		dataType : "text",
		success : function(data) {
			rowData.tempFlag = data;
			$("#" + subgridTableId).jqGrid('setRowData', id, rowData);
		}

	});
}

function parseRank(rank) {
	var rankStr = "NotSet";
	switch (rank) {
		case 1 : 
			rankStr = "Easy";
			break;
		case 3 : 
			rankStr = "Medium";
			break;
		case 5 : 
			rankStr = "Hard";
			break;
		case -2 : 
			rankStr = "Ignore";
			break;
	} 
	return rankStr;
}