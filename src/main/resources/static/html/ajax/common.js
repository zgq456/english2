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

//保留两位小数     
//功能：将浮点数四舍五入，取小数点后2位    
function toDecimal(x) {    
    var f = parseFloat(x);    
    if (isNaN(f)) {    
        return;    
    }    
    f = Math.round(x*100)/100;    
    f = f.toFixed(2);
    return f;    
}   

function GetArgsFromHref(sHref, sArgName)
{
      var args    = sHref.split("?");
      var retval = "";
    
      if(args[0] == sHref) /*参数为空*/
      {
           return retval; /*无需做任何处理*/
      }  
      var str = args[1];
      args = str.split("&");
      for(var i = 0; i < args.length; i ++)
      {
          str = args[i];
          var arg = str.split("=");
          if(arg.length <= 1) continue;
          if(arg[0] == sArgName) retval = arg[1]; 
      }
      return retval;
}

function toggleForkSen(spanObj, senId) {
	console.log("toggleForkSen senId:" + senId);
	var forkValue = $(spanObj).html().indexOf("glyphicon-star-empty") >= 0 ? 5 : 0; 
	if(forkValue == 0) {
		$(spanObj).html("&nbsp;<i class='ace-icon glyphicon glyphicon-star-empty'></i>");
	} else {
		$(spanObj).html("&nbsp;<i class='ace-icon glyphicon glyphicon-star'></i>");
	}					
	$.ajax({
		type : "POST",
		url : "../../toggleForkSen",
		data : {
			senId : senId,
			forkValue : forkValue
		},
		async : true,
		dataType : "json",
		success : function(data) {
			if(data == "0") {
				alert("please login first");
			}
			//rowData.tempRank = parseRank(newRank);
			//$("#grid-table").jqGrid('setRowData', id, rowData);
		},
		error : function(data) {
			alert("submit result error, please contact administrator");
		}

	});
}

function deleteArticle(obj, id) {
	bootbox.confirm("确定要删除该文章(ID:" + id + ")吗？", function(result) {
		if(result) {
			$
			.ajax({
				type : "POST",
				url : "../../deleteArticle",
				data : {
					id : id 
				},
				async : true,
				dataType : "text",
				success : function(data) {
					 alert("删除成功");
					 window.location.reload();
					 
				},
				error : function(data) {
					alert("submit result error, please contact administrator");
				}

			});
		}
	});
}



function showSenDetail(articleId) {
	window.location = "ajax.html?articleId=" + articleId + "#page/sentences2";
}

function showWordDetail(articleId) {
	window.location = "ajax.html?articleId=" + articleId + "#page/words2";
}



function toggleForkWord(spanObj, wordId) {
	console.log("toggleFork wordId:" + wordId);
	var forkValue = $(spanObj).html().indexOf("glyphicon-star-empty") >= 0 ? 1 : 0; 
	if(forkValue == 0) {
		$(spanObj).html("&nbsp;<i class='ace-icon glyphicon glyphicon-star-empty'></i>");
	} else {
		$(spanObj).html("&nbsp;<i class='ace-icon glyphicon glyphicon-star'></i>");
	}					
	$.ajax({
		type : "POST",
		url : "../../toggleWordFork",
		data : {
			wordId : wordId,
			forkValue : forkValue
		},
		async : true,
		dataType : "json",
		success : function(data) {
			if(data == "0") {
				alert("please login first");
			}
			//rowData.tempRank = parseRank(newRank);
			//$("#grid-table").jqGrid('setRowData', id, rowData);
		},
		error : function(data) {
			alert("submit result error, please contact administrator");
		}

	});
}

