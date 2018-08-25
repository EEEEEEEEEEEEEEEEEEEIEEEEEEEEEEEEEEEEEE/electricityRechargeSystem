package com.xuranus.main;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/main")
    public String mainPage(Model model, HttpSession session){
        model.addAttribute("username",session.getAttribute("username"));
        model.addAttribute("test","hahah");
        return "user-main";
    }

    @GetMapping("/account")
    public String account(Model model, HttpSession session){
        model.addAttribute("username",session.getAttribute("username"));
        model.addAttribute("title","用户缴费系统");
        int user_id = (int)session.getAttribute("user_id");
        String sql = "select * from user where user_id= "+user_id;
        List result = jdbcTemplate.query(sql, new RowMapper<Map>() {
            @Override
            public Map mapRow(ResultSet rs, int rowNum) throws SQLException {
                Map row = new HashMap();
                row.put("username", rs.getString("username"));
                row.put("balance",rs.getDouble("balance"));
                row.put("telephone",rs.getString("telephone"));
                row.put("register_date",rs.getString("register_date"));
                row.put("user_id",rs.getInt("user_id"));
                return row;
            }});
        if(result.size()!=1) {
            return "error";
        }

        Map line = (Map)result.get(0);
        model.addAttribute("title","我的账户");
        model.addAttribute("username",line.get("username"));
        model.addAttribute("balance",line.get("balance"));
        model.addAttribute("telephone",line.get("telephone"));
        model.addAttribute("register_date",line.get("register_date"));
        model.addAttribute("user_id",line.get("user_id"));

        sql = "select * from device where user_id = "+session.getAttribute("user_id");
        result = jdbcTemplate.query(sql, new RowMapper<Map>() {
            @Override
            public Map mapRow(ResultSet rs, int rowNum) throws SQLException {
                Map row = new HashMap();
                row.put("device_id", rs.getInt("device_id"));
                row.put("device_type",rs.getInt("device_type"));
                row.put("last_reading",rs.getDouble("last_reading"));
                row.put("address",rs.getString("address"));
                row.put("arrears",rs.getDouble("arrears"));
                return row;
            }});
        model.addAttribute("my_devices",result);
        //System.out.println(result);
        return "user-account";
    }

    @GetMapping("/query")
    public String queryThisMonth(Model model, HttpSession session){
        int user_id = (int)session.getAttribute("user_id");
        String sql = "select * from fee_payable where user_id="+user_id;
        List result = jdbcTemplate.query(sql, new RowMapper<Map>() {
            @Override
            public Map mapRow(ResultSet rs, int rowNum) throws SQLException {
                Map row = new HashMap();
                row.put("electric_degree",rs.getDouble("electric_degree"));
                row.put("total_fee",rs.getDouble("total_fee"));
                row.put("has_paid",rs.getDouble("has_paid"));
                row.put("should_pay",rs.getDouble("should_pay"));
                row.put("fee1",rs.getDouble("fee1"));
                row.put("fee2",rs.getDouble("fee2"));
                row.put("generate_date",rs.getDate("generate_date"));
                return row;
            }});
        if(result.size()!=1) {
            return "error";
        }
        else {
            Map bill = (Map) result.get(0);
            model.addAttribute("title", "本月账单查询");
            model.addAttribute("username",session.getAttribute("username"));
            model.addAttribute("bill", bill);
            return "user-query";
        }
    }



    @GetMapping("/bills")
    public String bills(Model model, HttpSession session){
        int user_id = (int)session.getAttribute("user_id");
        String sql = "select * from bill where bill_status=0 and user_id = "+user_id;
        List bills = jdbcTemplate.query(sql, new RowMapper<Map>() {
            @Override
            public Map mapRow(ResultSet rs, int rowNum) throws SQLException {
                Map row = new HashMap();
                //put something here...
                row.put("bill_id",rs.getInt("bill_id"));
                row.put("bill_status",rs.getInt("bill_status"));
                row.put("total_cost",rs.getDouble("total_cost"));
                row.put("principle",rs.getDouble("principle"));
                row.put("user_id",rs.getInt("user_id"));
                row.put("bill_generate_date",rs.getDate("bill_generate_date"));
                row.put("bill_start_date",rs.getDate("bill_start_date"));
                row.put("bill_payed_date",rs.getDate("bill_payed_date"));
                return row;
            }});
        //System.out.println(bills);
        model.addAttribute("title", "用户缴费系统");
        model.addAttribute("username",session.getAttribute("username"));
        model.addAttribute("bills", bills);
        return "user-bills";
    }



    @GetMapping("/recharge")//在线缴费
    public String recharge(Model model, HttpSession session){
        int user_id = (int)session.getAttribute("user_id");
        model.addAttribute("username",session.getAttribute("username"));
        model.addAttribute("title","用户缴费系统");

        //获取设备列表
        String sql = "select * from device where user_id="+user_id;
        List devices = jdbcTemplate.query(sql, new RowMapper<Map>() {
            @Override
            public Map mapRow(ResultSet rs, int rowNum) throws SQLException {
                Map row = new HashMap();
                row.put("device_id", rs.getInt("device_id"));
                row.put("device_type",rs.getInt("device_type"));
                row.put("last_reading",rs.getDouble("last_reading"));
                row.put("address",rs.getString("address"));
                row.put("arrears",rs.getDouble("arrears"));
                return row;
            }});
        model.addAttribute("my_devices",devices);
        //System.out.println(devices);


        //获取银行卡列表
        sql = "select * from bankcard where user_id="+user_id;
        List cards = jdbcTemplate.query(sql, new RowMapper<Map>() {
            @Override
            public Map mapRow(ResultSet rs, int rowNum) throws SQLException {
                Map row = new HashMap();
                row.put("bank_id", rs.getInt("bank_id"));
                row.put("bankcard_number",rs.getString("bankcard_number"));
                return row;
            }});
        //System.out.println(cards);
        model.addAttribute("my_cards",cards);
        return "user-recharge";
    }



    @GetMapping("/recharge-record")//历史缴费记录
    public String rechargeRecord(Model model, HttpSession session){
        int user_id = (int)session.getAttribute("user_id");
        String sql = "select * from recharge where user_id="+user_id;
        List result = jdbcTemplate.query(sql, new RowMapper<Map>() {
            @Override
            public Map mapRow(ResultSet rs, int rowNum) throws SQLException {
                Map row = new HashMap();
                    row.put("recharge_id",rs.getInt("recharge_id"));
                    row.put("recharge_date",rs.getDate("recharge_date"));
                    row.put("recharge_remark",rs.getString("recharge_remark"));
                    row.put("payment_seq_number",rs.getString("payment_seq_number"));
                    row.put("user_id",rs.getInt("user_id"));
                    row.put("device_id",rs.getInt("device_id"));
                return row;
            }});
        //System.out.println("hey : "+result);

        model.addAttribute("username",session.getAttribute("username"));
        model.addAttribute("title","用户缴费系统");
        model.addAttribute("historyRechargeRecord", result);
        return "user-recharge-record";
    }




}

