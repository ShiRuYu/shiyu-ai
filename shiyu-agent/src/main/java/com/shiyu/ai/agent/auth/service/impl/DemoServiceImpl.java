package com.shiyu.ai.agent.auth.service.impl;

import com.shiyu.ai.agent.auth.service.DemoService;
import com.shiyu.ai.agent.domain.vo.MenuAllVO;
import com.shiyu.ai.agent.domain.vo.PageResult;
import com.shiyu.ai.agent.domain.vo.ProductVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 演示服务实现类
 */
@Slf4j
@Service
public class DemoServiceImpl implements DemoService {

    @Override
    public List<MenuAllVO> getAllMenus() {
        log.info("返回模拟菜单数据");
        
        // 构建 Dashboard 菜单
        MenuAllVO dashboard = new MenuAllVO();
        dashboard.setName("Dashboard");
        dashboard.setPath("/dashboard");
        dashboard.setRedirect("/analytics");
        
        MenuAllVO.MetaVO dashboardMeta = new MenuAllVO.MetaVO();
        dashboardMeta.setOrder(-1);
        dashboardMeta.setTitle("page.dashboard.title");
        dashboard.setMeta(dashboardMeta);
        
        // Dashboard 子菜单 - Analytics
        MenuAllVO analytics = new MenuAllVO();
        analytics.setName("Analytics");
        analytics.setPath("/analytics");
        analytics.setComponent("/dashboard/analytics/index");
        
        MenuAllVO.MetaVO analyticsMeta = new MenuAllVO.MetaVO();
        analyticsMeta.setAffixTab(true);
        analyticsMeta.setTitle("page.dashboard.analytics");
        analytics.setMeta(analyticsMeta);
        
        // Dashboard 子菜单 - Workspace
        MenuAllVO workspace = new MenuAllVO();
        workspace.setName("Workspace");
        workspace.setPath("/workspace");
        workspace.setComponent("/dashboard/workspace/index");
        
        MenuAllVO.MetaVO workspaceMeta = new MenuAllVO.MetaVO();
        workspaceMeta.setTitle("page.dashboard.workspace");
        workspace.setMeta(workspaceMeta);
        
        dashboard.setChildren(Arrays.asList(analytics, workspace));
        
        // 构建 Demos 菜单
        MenuAllVO demos = new MenuAllVO();
        demos.setName("Demos");
        demos.setPath("/demos");
        demos.setRedirect("/demos/access");
        
        MenuAllVO.MetaVO demosMeta = new MenuAllVO.MetaVO();
        demosMeta.setIcon("ic:baseline-view-in-ar");
        demosMeta.setKeepAlive(true);
        demosMeta.setOrder(1000);
        demosMeta.setTitle("demos.title");
        demos.setMeta(demosMeta);
        
        // Demos 子菜单 - AccessDemos
        MenuAllVO accessDemos = new MenuAllVO();
        accessDemos.setName("AccessDemos");
        accessDemos.setPath("/demosaccess");
        accessDemos.setRedirect("/demos/access/page-control");
        
        MenuAllVO.MetaVO accessDemosMeta = new MenuAllVO.MetaVO();
        accessDemosMeta.setIcon("mdi:cloud-key-outline");
        accessDemosMeta.setTitle("demos.access.backendPermissions");
        accessDemos.setMeta(accessDemosMeta);
        
        // AccessDemos 子菜单 - PageControl
        MenuAllVO pageControl = new MenuAllVO();
        pageControl.setName("AccessPageControlDemo");
        pageControl.setPath("/demos/access/page-control");
        pageControl.setComponent("/demos/access/index");
        
        MenuAllVO.MetaVO pageControlMeta = new MenuAllVO.MetaVO();
        pageControlMeta.setIcon("mdi:page-previous-outline");
        pageControlMeta.setTitle("demos.access.pageAccess");
        pageControl.setMeta(pageControlMeta);
        
        // AccessDemos 子菜单 - ButtonControl
        MenuAllVO buttonControl = new MenuAllVO();
        buttonControl.setName("AccessButtonControlDemo");
        buttonControl.setPath("/demos/access/button-control");
        buttonControl.setComponent("/demos/access/button-control");
        
        MenuAllVO.MetaVO buttonControlMeta = new MenuAllVO.MetaVO();
        buttonControlMeta.setIcon("mdi:button-cursor");
        buttonControlMeta.setTitle("demos.access.buttonControl");
        buttonControl.setMeta(buttonControlMeta);
        
        // AccessDemos 子菜单 - MenuVisible403
        MenuAllVO menuVisible403 = new MenuAllVO();
        menuVisible403.setName("AccessMenuVisible403Demo");
        menuVisible403.setPath("/demos/access/menu-visible-403");
        menuVisible403.setComponent("/demos/access/menu-visible-403");
        
        MenuAllVO.MetaVO menuVisible403Meta = new MenuAllVO.MetaVO();
        menuVisible403Meta.setAuthority(Arrays.asList("no-body"));
        menuVisible403Meta.setIcon("mdi:button-cursor");
        menuVisible403Meta.setMenuVisibleWithForbidden(true);
        menuVisible403Meta.setTitle("demos.access.menuVisible403");
        menuVisible403.setMeta(menuVisible403Meta);
        
        // AccessDemos 子菜单 - SuperVisible
        MenuAllVO superVisible = new MenuAllVO();
        superVisible.setName("AccessSuperVisibleDemo");
        superVisible.setPath("/demos/access/super-visible");
        superVisible.setComponent("/demos/access/super-visible");
        
        MenuAllVO.MetaVO superVisibleMeta = new MenuAllVO.MetaVO();
        superVisibleMeta.setIcon("mdi:button-cursor");
        superVisibleMeta.setTitle("demos.access.superVisible");
        superVisible.setMeta(superVisibleMeta);
        
        accessDemos.setChildren(Arrays.asList(pageControl, buttonControl, menuVisible403, superVisible));
        demos.setChildren(Arrays.asList(accessDemos));
        
        return Arrays.asList(dashboard, demos);
    }

    @Override
    public PageResult<ProductVO> getTableList(Integer page, Integer pageSize, String category, Date start, Date end) {
        log.info("获取表格数据列表 - page: {}, pageSize: {}, category: {}, start: {}, end: {}", 
                page, pageSize, category, start, end);
        
        // 生成模拟数据
        List<ProductVO> allProducts = generateMockProducts();
        
        // 过滤数据
        List<ProductVO> filteredProducts = allProducts.stream()
                .filter(product -> category == null || category.isEmpty() || category.equals(product.getCategory()))
                .filter(product -> start == null || product.getReleaseDate() == null || !product.getReleaseDate().before(start))
                .filter(product -> end == null || product.getReleaseDate() == null || !product.getReleaseDate().after(end))
                .collect(Collectors.toList());
        
        // 计算总数
        long total = filteredProducts.size();
        
        // 分页处理
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, filteredProducts.size());
        
        List<ProductVO> pagedProducts = filteredProducts.subList(
                Math.max(0, fromIndex), 
                Math.min(toIndex, filteredProducts.size())
        );
        
        return new PageResult<>(pagedProducts, total);
    }
    
    /**
     * 生成模拟产品数据
     */
    private List<ProductVO> generateMockProducts() {
        List<ProductVO> products = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        try {
            // 产品 1
            ProductVO p1 = new ProductVO();
            p1.setId("7d1289b0-2a21-4c91-8dcc-e179713de885");
            p1.setImageUrl("https://cdn.jsdelivr.net/gh/faker-js/assets-person-portrait/male/512/31.jpg");
            p1.setImageUrl2("https://avatars.githubusercontent.com/u/64056710");
            p1.setOpen(false);
            p1.setStatus("success");
            p1.setProductName("Modern Marble Chips");
            p1.setPrice("673.99");
            p1.setCurrency("ALL");
            p1.setQuantity(55);
            p1.setAvailable(true);
            p1.setCategory("Shoes");
            p1.setReleaseDate(sdf.parse("2025-07-13T11:51:29.471Z"));
            p1.setRating(4.6562011982533775);
            p1.setDescription("New Shirt model with 13 GB RAM, 487 GB storage, and weighty features");
            p1.setWeight(7.125494367450778);
            p1.setColor("plum");
            p1.setInProduction(false);
            p1.setTags(Arrays.asList("Tasty", "Incredible", "Practical"));
            products.add(p1);
            
            // 产品 2
            ProductVO p2 = new ProductVO();
            p2.setId("8af6f270-e98b-484b-b5aa-894420877bef");
            p2.setImageUrl("https://avatars.githubusercontent.com/u/58503171");
            p2.setImageUrl2("https://cdn.jsdelivr.net/gh/faker-js/assets-person-portrait/female/512/20.jpg");
            p2.setOpen(true);
            p2.setStatus("success");
            p2.setProductName("Ergonomic Silk Chips");
            p2.setPrice("953.49");
            p2.setCurrency("NAD");
            p2.setQuantity(33);
            p2.setAvailable(false);
            p2.setCategory("Music");
            p2.setReleaseDate(sdf.parse("2025-06-22T12:05:26.327Z"));
            p2.setRating(3.8790119728627186);
            p2.setDescription("Savor the golden essence in our Mouse, designed for quiet culinary adventures");
            p2.setWeight(8.986973755872356);
            p2.setColor("salmon");
            p2.setInProduction(false);
            p2.setTags(Arrays.asList("Awesome", "Frozen", "Electronic"));
            products.add(p2);
            
            // 产品 3
            ProductVO p3 = new ProductVO();
            p3.setId("106b8802-fb91-4dd7-b128-bec93c039b3e");
            p3.setImageUrl("https://cdn.jsdelivr.net/gh/faker-js/assets-person-portrait/male/512/63.jpg");
            p3.setImageUrl2("https://avatars.githubusercontent.com/u/13004656");
            p3.setOpen(true);
            p3.setStatus("warning");
            p3.setProductName("Electronic Silk Salad");
            p3.setPrice("579.19");
            p3.setCurrency("UAH");
            p3.setQuantity(16);
            p3.setAvailable(true);
            p3.setCategory("Automotive");
            p3.setReleaseDate(sdf.parse("2025-04-26T02:46:05.996Z"));
            p3.setRating(4.99642980666208);
            p3.setDescription("Our crispy-inspired Chicken brings a taste of luxury to your foolish lifestyle");
            p3.setWeight(1.763221691838134);
            p3.setColor("salmon");
            p3.setInProduction(true);
            p3.setTags(Arrays.asList("Rustic", "Refined", "Rustic"));
            products.add(p3);
            
            // 产品 4
            ProductVO p4 = new ProductVO();
            p4.setId("e0abff1c-1cac-4b31-a538-5c4f7de7af1b");
            p4.setImageUrl("https://cdn.jsdelivr.net/gh/faker-js/assets-person-portrait/female/512/35.jpg");
            p4.setImageUrl2("https://cdn.jsdelivr.net/gh/faker-js/assets-person-portrait/male/512/98.jpg");
            p4.setOpen(false);
            p4.setStatus("warning");
            p4.setProductName("Unbranded Plastic Towels");
            p4.setPrice("222.89");
            p4.setCurrency("PKR");
            p4.setQuantity(6);
            p4.setAvailable(true);
            p4.setCategory("Books");
            p4.setReleaseDate(sdf.parse("2025-03-24T23:17:19.927Z"));
            p4.setRating(4.612099907403319);
            p4.setDescription("The Elissa Chips is the latest in a series of back products from Reichert, Rippin and Lindgren");
            p4.setWeight(9.481369377484683);
            p4.setColor("indigo");
            p4.setInProduction(false);
            p4.setTags(Arrays.asList("Oriental", "Intelligent", "Unbranded"));
            products.add(p4);
            
            // 产品 5
            ProductVO p5 = new ProductVO();
            p5.setId("2720bc24-dd67-40f2-acf4-691bf70c788c");
            p5.setImageUrl("https://cdn.jsdelivr.net/gh/faker-js/assets-person-portrait/female/512/62.jpg");
            p5.setImageUrl2("https://avatars.githubusercontent.com/u/43989546");
            p5.setOpen(true);
            p5.setStatus("error");
            p5.setProductName("Sleek Wooden Soap");
            p5.setPrice("369.20");
            p5.setCurrency("TZS");
            p5.setQuantity(7);
            p5.setAvailable(false);
            p5.setCategory("Books");
            p5.setReleaseDate(sdf.parse("2025-06-10T04:13:26.465Z"));
            p5.setRating(2.1704395649998633);
            p5.setDescription("The Amara Shirt is the latest in a series of pastel products from Murazik - Purdy");
            p5.setWeight(6.599813131446328);
            p5.setColor("white");
            p5.setInProduction(false);
            p5.setTags(Arrays.asList("Elegant", "Sleek", "Generic"));
            products.add(p5);
            
        } catch (ParseException e) {
            log.error("日期解析异常", e);
        }
        
        // 生成更多模拟数据以达到100条
        while (products.size() < 100) {
            ProductVO product = generateRandomProduct(products.size() + 1);
            products.add(product);
        }
        
        return products;
    }
    
    /**
     * 生成随机产品数据
     */
    private ProductVO generateRandomProduct(int index) {
        ProductVO product = new ProductVO();
        product.setId(UUID.randomUUID().toString());
        product.setImageUrl("https://cdn.jsdelivr.net/gh/faker-js/assets-person-portrait/male/512/" + (index % 100) + ".jpg");
        product.setImageUrl2("https://avatars.githubusercontent.com/u/" + (10000000 + index));
        product.setOpen(index % 2 == 0);
        product.setStatus(index % 3 == 0 ? "success" : (index % 3 == 1 ? "warning" : "error"));
        product.setProductName("Product " + index);
        product.setPrice(String.format("%.2f", Math.random() * 1000));
        product.setCurrency("USD");
        product.setQuantity((int) (Math.random() * 100));
        product.setAvailable(index % 2 == 0);
        
        String[] categories = {"Shoes", "Music", "Automotive", "Books", "Kids", "Games", "Tools", "Garden", "Clothing", "Computers"};
        product.setCategory(categories[index % categories.length]);
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -index);
        product.setReleaseDate(cal.getTime());
        
        product.setRating(Math.random() * 5);
        product.setDescription("Description for product " + index);
        product.setWeight(Math.random() * 10);
        
        String[] colors = {"red", "blue", "green", "yellow", "orange", "purple", "pink", "black", "white"};
        product.setColor(colors[index % colors.length]);
        
        product.setInProduction(index % 3 == 0);
        product.setTags(Arrays.asList("Tag1", "Tag2", "Tag3"));
        
        return product;
    }
}
