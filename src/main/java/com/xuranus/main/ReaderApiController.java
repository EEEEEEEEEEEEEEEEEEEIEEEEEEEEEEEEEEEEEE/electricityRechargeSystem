package com.xuranus.main;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/reader/api")
public class ReaderApiController {

    @Autowired
    private JdbcTemplate jdbcTemplate;


    @RequestMapping("/confirm-record")
    public Map confirmRecord(HttpSession session, HttpServletRequest req, HttpServletResponse res) {
        int device_id = Integer.parseInt(req.getParameter("device_id"));
        double current_reading = Double.parseDouble(req.getParameter("reading"));

        String sql = "select * from device where device_id = " + device_id;
        List result = jdbcTemplate.query(sql, new RowMapper<Map>() {
            @Override
            public Map mapRow(ResultSet rs, int rowNum) throws SQLException {
                Map row = new HashMap();
                row.put("last_reading", rs.getDouble("last_reading"));
                row.put("device_id", rs.getInt("device_id"));
                row.put("address", rs.getString("address"));
                row.put("reader_name", session.getAttribute("meter_reader_name"));
                return row;
            }
        });
        if (result.size() != 1 || current_reading < (double) ((Map) result.get(0)).get("last_reading")) {
            Map resJson = new HashMap();
            resJson.put("success", false);
            resJson.put("msg", "读数或设备号不正确");
            return resJson;
        } else {
            Map resJson = (Map) result.get(0);
            resJson.put("success", true);
            resJson.put("current_reading", current_reading);
            return resJson;
        }
    }

    @RequestMapping("/save-record")
    public Map saveRecord(HttpSession session, HttpServletRequest req, HttpServletResponse res) {
        String device_id = req.getParameter("device-id");
        String meter_reader_name = req.getParameter("reader");
        String address = req.getParameter("address");
        String currentReading = req.getParameter("current-reading");

        //创建抄表记录表
        String sql = "insert into read_record (device_id,reading,meter_reader_id,meter_reader_name,record_date) values "
                +"("+device_id+","+currentReading+","+session.getAttribute("meter_reader_id")+",'"+meter_reader_name+"',NOW())";
        //System.out.println("SQL:"+sql);

        //判断上个月有没有欠费 生成欠费表

        //计算本月电费 更新应缴费表


        Map resJson = new HashMap();
        if(jdbcTemplate.update(sql)>0) {
            resJson.put("success",true);
        }
        else {
            resJson.put("success", false);
            resJson.put("msg","操作失败");
        }
        return resJson;
    }

}
