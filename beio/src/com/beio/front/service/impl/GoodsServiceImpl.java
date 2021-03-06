package com.beio.front.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.beio.base.entity.SysMember;
import com.beio.base.entity.SysPay;
import com.beio.base.service.impl.BaseIbatisServiceImpl;
import com.beio.base.util.ComUtil;
import com.beio.base.util.ConfigUtil;
import com.beio.base.vo.Root;
import com.beio.front.entity.GdsBrand;
import com.beio.front.entity.GdsBuycart;
import com.beio.front.entity.GdsClassify;
import com.beio.front.entity.GdsGoods;
import com.beio.front.entity.GdsOrder;
import com.beio.front.service.GoodsService;
import com.beio.front.vo.BuycartVO;
import com.beio.front.vo.CartInfoVO;
import com.beio.front.vo.ClassifyVO;
import com.beio.front.vo.DetailVO;
import com.beio.front.vo.GoodsVO;
import com.beio.front.vo.IndexInfoVO;
import com.beio.front.vo.OrderVO;
import com.beio.front.vo.PreOrderVO;
import com.beio.front.vo.SearchInfoVO;
import com.beio.front.vo.SettlementVO;
import com.beio.front.vo.TopInfoVO;

/**
 * 商品业务逻辑实现
 * @author zhs
 * @date 2017-04-09
 * @version 1.0.0
 */
@SuppressWarnings({ "unchecked" })
public class GoodsServiceImpl extends BaseIbatisServiceImpl implements GoodsService {

	@Override
	public TopInfoVO queryTopInfo(SysMember member) throws Exception {
		TopInfoVO top = new TopInfoVO();
		// 装载会员信息
		top.setMember(member);
		// 装载热搜关键字
		top.setSearchs(selectList("goods.querySearchs"));
		// 装载商品分类
		top.setClassifys(selectList("goods.queryClassifys"));
		// 装载购物车数量
		top.setCartNum(String.valueOf(selectOne("goods.buycartQuantity", member)));
		// TODO Auto-generated method stub
		return top;
	}
	
	@Override
	public IndexInfoVO queryIndexInfo(SysMember member) throws Exception {
		IndexInfoVO index = new IndexInfoVO();
		// 装载会员信息
		index.setMember(member);
		index.setBanners(selectList("goods.queryBanners"));
		GdsClassify classify = new GdsClassify();
		classify.setLevel("1");
		classify.setShowIndex("1");
		index.setClassifys(queryClassify(classify));
		return index;
	}
	
	@Override
	public SearchInfoVO querySearchInfo(SearchInfoVO searchInfoVO) throws Exception {
		Set<String> brands = new HashSet<String>();
		searchInfoVO.setClassifys(selectList("goods.queryClassifysBySearch", searchInfoVO));
		GdsClassify classify = new GdsClassify();
		classify.setId("");
		classify.setName("全部");
		searchInfoVO.getClassifys().add(0, classify);
		searchInfoVO.setNavClassifys(selectList("goods.queryClassifyNavBar", searchInfoVO));
		queryGoods(brands, searchInfoVO);
		if (ComUtil.isNotEmpty(brands)) {
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("brands", brands);
			searchInfoVO.setBrands(selectList("goods.queryBrands", param));
		}else {
			searchInfoVO.setBrands(new ArrayList<GdsBrand>());
		}
		GdsBrand brand = new GdsBrand();
		brand.setId("");
		brand.setName("全部");
		searchInfoVO.getBrands().add(0, brand);
		return searchInfoVO;
	}
	
	@Override
	public GoodsVO queryGoodsInfo(GoodsVO goodsVO) throws Exception {
		// TODO Auto-generated method stub
		goodsVO = (GoodsVO) selectOne("goods.queryGoodsByID", goodsVO.getId());
		goodsVO.setShows(selectList("goods.queryShowsByGoods", goodsVO));
		goodsVO.setDetails(selectList("goods.queryDetailsByGoods", goodsVO));
		return goodsVO;
	}
	
	@Override
	public int joinBuycat(GdsBuycart gdsBuycart) throws Exception {
		// TODO Auto-generated method stub
		if (update("goods.updtBuycart", gdsBuycart) < 1) {
			if (insert("goods.joinBuycart", gdsBuycart) < 1) {
				return 0;
			}
		}
		return 1;
	}
	
	@Override
	public CartInfoVO queryBuycart(CartInfoVO cartInfoVO) throws Exception {
		// TODO Auto-generated method stub
		cartInfoVO.setBuycarts(selectList("goods.queryBuycart", cartInfoVO.getMember()));
		if (ComUtil.isNotEmpty(cartInfoVO.getBuycarts())) {
			for (BuycartVO cart : cartInfoVO.getBuycarts()) {
				cart.setGoods(queryGoods(cart.getGoodsID()));
			}
		}
		return cartInfoVO;
	}
	
	@Override
	public SettlementVO settlement(SettlementVO settlementVO) throws Exception {
		// TODO Auto-generated method stub
		if (settlementVO.getMember() != null) {
			settlementVO.setAddress(selectList("sys.queryAddrByMID", settlementVO.getMember().getId()));
		}
		settlementVO.setCarts(selectList("goods.settlement", settlementVO));
		if (ComUtil.isNotEmpty(settlementVO.getCarts())) {
			for (BuycartVO cart : settlementVO.getCarts()) {
				cart.setGoods(queryGoods(cart.getGoodsID()));
			}
		}
		return settlementVO;
	}
	
	@Override
	public OrderVO myOrder(OrderVO orderVO) throws Exception {
		// TODO Auto-generated method stub
		selectPage("goods.queryOrder", orderVO);
		if (ComUtil.isNotEmpty(orderVO.getPageList())) {
			for (Object order : orderVO.getPageList()) {
				((OrderVO)order).setShows(selectList("goods.queryShowsByGoods", ((OrderVO)order).getGoodsID()));
			}
		}
		return orderVO;
	}
	
	/**
	 * 查询商品分类
	 * @param classify
	 * @return
	 * @throws Exception
	 */
	private List<ClassifyVO> queryClassify(GdsClassify classify) throws Exception{
		List<ClassifyVO> cvs = selectList("goods.queryClassifys", classify);
		if (ComUtil.isNotEmpty(cvs)) {
			for (ClassifyVO classifyVO : cvs) {
				classifyVO.setGoods(queryGoods(classifyVO));
			}
		}
		ClassifyVO hot = new ClassifyVO();
		hot.setName("热门商品");
		hot.setGoods(queryGoods(hot));
		cvs.add(0, hot);
		return cvs;
	}
	
	/**
	 * 查询商品信息
	 * @param classify
	 * @return
	 * @throws Exception
	 */
	private List<GoodsVO> queryGoods(GdsClassify classify) throws Exception{
		List<GoodsVO> gvs = selectList("goods.queryGoodsByClassify", classify);
		if (ComUtil.isNotEmpty(gvs)) {
			for (GoodsVO goodsVO : gvs) {
				goodsVO.setShows(selectList("goods.queryShowsByGoods", goodsVO));
			}
		}
		return gvs;
	}
	
	/**
	 * 查询商品信息
	 * @param classify
	 * @return
	 * @throws Exception
	 */
	private SearchInfoVO queryGoods(Set<String> brands, SearchInfoVO searchInfoVO) throws Exception{
		selectPage("goods.queryGoodsBySearch", searchInfoVO);
		if (ComUtil.isNotEmpty(searchInfoVO.getPageList())) {
			for (Object goodsVO : searchInfoVO.getPageList()) {
				brands.add(((GoodsVO)goodsVO).getBrandID());
				((GoodsVO)goodsVO).setShows(selectList("goods.queryShowsByGoods", goodsVO));
			}
		}
		return searchInfoVO;
	}
	
	/**
	 * 查询商品信息
	 * @param classify
	 * @return
	 * @throws Exception
	 */
	private GoodsVO queryGoods(String goodsID) throws Exception{
		GoodsVO goods = (GoodsVO) selectOne("goods.queryGoodsByID", goodsID);
		if (goods != null) {
			goods.setShows(selectList("goods.queryShowsByGoods", goods));
		}
		return goods;
	}
	
	@Override
	public Root preOrder(PreOrderVO preOrderVO) throws Exception {
		// TODO Auto-generated method stub
		// 订单为空
		if (preOrderVO == null || ComUtil.isEmpty(preOrderVO.getOrders())) {
			return new Root("301");
		}
		// 订单总额
		Float totalPrice = 0f;
		int k = 0;
		// 校验商品准确性
		for (OrderVO order : preOrderVO.getOrders()) {
			// 查询订单对应商品
			GdsGoods goods = (GdsGoods) selectOne("goods.queryGoodsByID", order.getGoodsID());
			// 商品不存在
			if (goods == null) {
				return new Root("303");
			}
			// 价格不对等
			if (!Float.valueOf(goods.getmPrice()).equals(Float.valueOf(order.getGoodsPrice()))) {
				return new Root("304");
			}
			// 库存量不足
			if (Integer.valueOf(goods.getStock()) < Integer.valueOf(order.getGoodsQuantity())) {
				return new Root("305");
			}
			// 商品金额异常
			if (!Float.valueOf(Float.valueOf(goods.getmPrice()) * Integer.valueOf(order.getGoodsQuantity())).equals(Float.valueOf(order.getTotalPrice()))) {
				return new Root("306");
			}
			// 运费不对等
			if (!Float.valueOf(order.getGoodsFreight()).equals(Float.valueOf(selectOne("goods.validFreight", order).toString()))) {
				return new Root("307");
			}
			// 累计总费用
			totalPrice += Float.valueOf(order.getTotalPrice()) + Float.valueOf(order.getGoodsFreight());
			// 生成订单号
			order.setOrderNo(ComUtil.orderNo(++k));
			// 填充购买人
			order.setBuyerID(preOrderVO.getCreator());
		}
		// 元转分
		String total_fee = String.valueOf(BigDecimal.valueOf(totalPrice).multiply(new BigDecimal(100)).intValue());
		preOrderVO.setCategory("0");
		// 保存下单信息
		insert("sys.pay", preOrderVO);
		// 获取支付标识
		preOrderVO.setId(String.valueOf(selectOne("queryid")));
		// 微信统一下单参数
		Map<String, String> param = new TreeMap<String, String>();
		param.put("appid", ConfigUtil.getProperties("wx.appid"));
		param.put("body", "快客林-购买商品");
		param.put("mch_id", ConfigUtil.getProperties("wx.mch_id"));
		param.put("nonce_str", ComUtil.uuid());
		param.put("notify_url", "http://localhost:8080/beio/pay/notify");
		param.put("out_trade_no", preOrderVO.getId());
		param.put("total_fee", "1");
		param.put("trade_type", "NATIVE");
		param.put("sign", ComUtil.signWX(param, ConfigUtil.getProperties("wx.api_key")));
		String sender_str = ComUtil.installXML(param);
		String return_str = ComUtil.httpPost("https://api.mch.weixin.qq.com/pay/unifiedorder", sender_str);
		// 解析响应参数
		Map<String, String> map = ComUtil.parseXML(return_str);
		// 填充订单参数
		preOrderVO.setTotal_fee(total_fee);
		preOrderVO.setSender_str(sender_str);
		preOrderVO.setReturn_str(return_str);
		preOrderVO.setCode_url(map.get("code_url"));
		preOrderVO.setPrepay_id(map.get("prepay_id"));
		preOrderVO.setReturn_msg(map.get("return_msg"));
		preOrderVO.setReturn_code(map.get("return_code"));
		update("sys.unifiedorder", preOrderVO);
		// 商品下单
		update("goods.preGoods", preOrderVO);
		// 购物车下单
		update("goods.preBuycart", preOrderVO);
		// 购物订单下单
		insert("goods.preOrder", preOrderVO);
		// 返回成功结果
		return new Root(preOrderVO, "200");
	}

	@Override
	public SettlementVO freight(SettlementVO settlementVO) throws Exception {
		// TODO Auto-generated method stub
		settlementVO.setCarts(selectList("goods.freight", settlementVO));
		return settlementVO;
	}

	@Override
	public Root payOrder(SysPay pay) throws Exception {
		// TODO Auto-generated method stub
		// 微信查询订单参数
		Map<String, String> param = new TreeMap<String, String>();
		param.put("appid", ConfigUtil.getProperties("wx.appid"));
		param.put("mch_id", ConfigUtil.getProperties("wx.mch_id"));
		param.put("out_trade_no", pay.getId());
		param.put("nonce_str", ComUtil.uuid());
		param.put("sign", ComUtil.signWX(param, ConfigUtil.getProperties("wx.api_key")));
		String sender_str = ComUtil.installXML(param);
		String return_str = ComUtil.httpPost("https://api.mch.weixin.qq.com/pay/orderquery", sender_str);
		// 解析下单响应参数
		Map<String, String> map = ComUtil.parseXML(return_str);
		// 判断支付结果
		if ("SUCCESS".equals(map.get("return_code"))) {
			if ("SUCCESS".equals(map.get("result_code"))) {
				// 支付成功
				if ("SUCCESS".equals(map.get("trade_state")) || "REFUND".equals(map.get("trade_state"))
						|| "CLOSED".equals(map.get("trade_state")) || "REVOKED".equals(map.get("trade_state"))
						|| "PAYERROR".equals(map.get("trade_state"))) {
					if ("SUCCESS".equals(map.get("trade_state"))) {
						pay.setStatus("1");
						update("goods.payOrder", pay);
					}else {
						pay.setStatus("2");
					}
					pay.setPay_str(return_str);
					pay.setTrade_state(map.get("trade_state"));
					// 更新支付状态
					update("sys.orderquery", pay);
				}
			}
		}
		return new Root(map.get("trade_state"), "200");
	}

	@Override
	public Root cancelOrder(OrderVO orderVO) throws Exception {
		// TODO Auto-generated method stub
		// 还原商品库存
		update("goods.cancelGoods", orderVO);
		// 取消支付订单
		update("goods.cancelOrder", orderVO);
		return new Root("200");
	}

	@Override
	public Root mergePay(PreOrderVO preOrderVO) throws Exception {
		// TODO Auto-generated method stub
		// 订单为空
		if (preOrderVO == null || ComUtil.isEmpty(preOrderVO.getOrders())) {
			return new Root("301");
		}
		// 订单总额
		Float totalPrice = 0f;
		// 校验商品准确性
		for (OrderVO order : preOrderVO.getOrders()) {
			GdsOrder o = (GdsOrder) selectOne("goods.queryOrderByID", order.getId());
			totalPrice += Float.valueOf(o.getTotalPrice()) + Float.valueOf(o.getGoodsFreight());
		}
		// 元转分
		String total_fee = String.valueOf(BigDecimal.valueOf(totalPrice).multiply(new BigDecimal(100)).intValue());
		// 创建支付对象
		preOrderVO.setCategory("0");
		// 保存下单信息
		insert("sys.pay", preOrderVO);
		// 获取支付标识
		preOrderVO.setId(String.valueOf(selectOne("queryid")));
		// 微信统一下单参数
		Map<String, String> param = new TreeMap<String, String>();
		param.put("appid", ConfigUtil.getProperties("wx.appid"));
		param.put("body", "快客林-购买商品");
		param.put("mch_id", ConfigUtil.getProperties("wx.mch_id"));
		param.put("nonce_str", ComUtil.uuid());
		param.put("notify_url", "http://localhost:8080/beio/pay/notify");
		param.put("out_trade_no", preOrderVO.getId());
		param.put("total_fee", "1");
		param.put("trade_type", "NATIVE");
		param.put("sign", ComUtil.signWX(param, ConfigUtil.getProperties("wx.api_key")));
		String sender_str = ComUtil.installXML(param);
		String return_str = ComUtil.httpPost("https://api.mch.weixin.qq.com/pay/unifiedorder", sender_str);
		// 解析响应参数
		Map<String, String> map = ComUtil.parseXML(return_str);
		// 填充订单参数
		preOrderVO.setTotal_fee(total_fee);
		preOrderVO.setSender_str(sender_str);
		preOrderVO.setReturn_str(return_str);
		preOrderVO.setCode_url(map.get("code_url"));
		preOrderVO.setPrepay_id(map.get("prepay_id"));
		preOrderVO.setReturn_msg(map.get("return_msg"));
		preOrderVO.setReturn_code(map.get("return_code"));
		update("sys.unifiedorder", preOrderVO);
		// 购物订单下单
		insert("goods.mergeOrder", preOrderVO);
		// 返回成功结果
		return new Root(preOrderVO, "200");
	}

	@Override
	public Root detail(DetailVO detailVO) throws Exception {
		// TODO Auto-generated method stub
		detailVO = (DetailVO) selectOne("goods.queryDetail", detailVO);
		detailVO.setServices(selectList("goods.queryService", detailVO));
		return new Root(detailVO, "200");
	}
	
}
