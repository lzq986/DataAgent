-- 初始化数据文件
-- 只在表为空时插入示例数据

-- 智能体示例数据
INSERT IGNORE INTO `agent` (`id`, `name`, `description`, `avatar`, `status`, `prompt`, `category`, `admin_id`, `tags`, `create_time`, `update_time`) VALUES
(1, '中国人口GDP数据智能体', '专门处理中国人口和GDP相关数据查询分析的智能体', '/avatars/china-gdp-agent.png', 'draft', '你是一个专业的数据分析助手，专门处理中国人口和GDP相关的数据查询。请根据用户的问题，生成准确的SQL查询语句。', '数据分析', 2100246635, '人口数据,GDP分析,经济统计', NOW(), NOW()),
(2, '销售数据分析智能体', '专注于销售数据分析和业务指标计算的智能体', '/avatars/sales-agent.png', 'draft', '你是一个销售数据分析专家，能够帮助用户分析销售趋势、客户行为和业务指标。', '业务分析', 2100246635, '销售分析,业务指标,客户分析', NOW(), NOW()),
(3, '财务报表智能体', '专门处理财务数据和报表分析的智能体', '/avatars/finance-agent.png', 'draft', '你是一个财务分析专家，专门处理财务数据查询和报表生成。', '财务分析', 2100246635, '财务数据,报表分析,会计', NOW(), NOW()),
(4, '库存管理智能体', '专注于库存数据管理和供应链分析的智能体', '/avatars/inventory-agent.png', 'draft', '你是一个库存管理专家，能够帮助用户查询库存状态、分析供应链数据。', '供应链', 2100246635, '库存管理,供应链,物流', NOW(), NOW());

-- 业务知识示例数据
INSERT IGNORE INTO `business_knowledge` (`id`, `business_term`, `description`, `synonyms`, `is_recall`, `data_set_id`, `agent_id`, `created_time`, `updated_time`) VALUES 
(1, 'Customer Satisfaction', 'Measures how satisfied customers are with the service or product.', 'customer happiness, client contentment', 0, 'dataset_001', 1, NOW(), NOW()),
(2, 'Net Promoter Score', 'A measure of the likelihood of customers recommending a company to others.', 'NPS, customer loyalty score', 0, 'dataset_002', 1, NOW(), NOW()),
(3, 'Customer Retention Rate', 'The percentage of customers who continue to use a service over a given period.', 'retention, customer loyalty', 0, 'dataset_003', 2, NOW(), NOW());

-- 语义模型示例数据
INSERT IGNORE INTO `semantic_model` (`id`, `agent_id`, `field_name`, `synonyms`, `origin_name`, `description`, `origin_description`, `type`, `created_time`, `updated_time`, `is_recall`, `status`) VALUES 
(1, 1, 'customerSatisfactionScore', 'satisfaction score, customer rating', 'csat_score', 'Customer satisfaction rating from 1-10', 'Customer satisfaction score', 'integer', NOW(), NOW(), 0, 0),
(2, 1, 'netPromoterScore', 'NPS, promoter score', 'nps_value', 'Net Promoter Score from -100 to 100', 'NPS calculation result', 'integer', NOW(), NOW(), 0, 0),
(3, 2, 'customerRetentionRate', 'retention rate, loyalty rate', 'retention_pct', 'Percentage of retained customers', 'Customer retention percentage', 'decimal', NOW(), NOW(), 0, 0);

-- 智能体知识示例数据
INSERT IGNORE INTO `agent_knowledge` (`id`, `agent_id`, `title`, `content`, `type`, `category`, `tags`, `status`, `source_url`, `file_type`, `embedding_status`, `creator_id`, `create_time`, `update_time`) VALUES
(1, 1, '中国人口统计数据说明', '中国人口统计数据包含了历年的人口总数、性别比例、年龄结构、城乡分布等详细信息。数据来源于国家统计局，具有权威性和准确性。查询时请注意数据的时间范围和统计口径。', 'document', '数据说明', '人口统计,数据源,统计局', 'inactive', 'http://stats.gov.cn/population', 'text', 'pending', 2100246635, NOW(), NOW()),
(2, 1, 'GDP数据使用指南', 'GDP（国内生产总值）数据反映了国家经济发展水平。包含名义GDP、实际GDP、GDP增长率等指标。数据按季度和年度进行统计，支持按地区、行业进行分类查询。', 'document', '使用指南', 'GDP,经济指标,增长率', 'inactive', 'http://stats.gov.cn/gdp', 'text', 'pending', 2100246635, NOW(), NOW()),
(3, 1, '常见查询问题', '问：如何查询2023年的人口数据？\n答：可以使用"SELECT * FROM population WHERE year = 2023"进行查询。\n\n问：如何计算GDP增长率？\n答：GDP增长率 = (当年GDP - 上年GDP) / 上年GDP * 100%', 'qa', '常见问题', '查询示例,FAQ,SQL语句', 'inactive', NULL, 'text', 'pending', 2100246635, NOW(), NOW()),
(4, 2, '销售数据字段说明', '销售数据表包含以下关键字段：\n- sales_amount：销售金额\n- customer_id：客户ID\n- product_id：产品ID\n- sales_date：销售日期\n- region：销售区域\n- sales_rep：销售代表', 'document', '数据字典', '销售数据,字段说明,数据结构', 'inactive', NULL, 'text', 'pending', 2100246635, NOW(), NOW()),
(5, 2, '客户分析指标体系', '客户分析包含多个维度：\n1. 客户价值分析：RFM模型（最近购买时间、购买频次、购买金额）\n2. 客户生命周期：新客户、活跃客户、流失客户\n3. 客户满意度：NPS评分、满意度调研\n4. 客户行为分析：购买偏好、渠道偏好', 'document', '分析方法', '客户分析,RFM,生命周期,满意度', 'inactive', NULL, 'text', 'pending', 2100246635, NOW(), NOW()),
(6, 3, '财务报表模板', '标准财务报表包含：\n1. 资产负债表：反映企业财务状况\n2. 利润表：反映企业经营成果\n3. 现金流量表：反映企业现金流动情况\n4. 所有者权益变动表：反映股东权益变化', 'document', '报表模板', '财务报表,资产负债表,利润表,现金流', 'inactive', 'https://finance.example.com/templates', 'pdf', 'pending', 2100246635, NOW(), NOW()),
(7, 4, '库存管理最佳实践', '库存管理的核心要点：\n1. 安全库存设置：确保不断货\n2. ABC分类管理：重点管理A类物料\n3. 先进先出原则：避免库存积压\n4. 定期盘点：确保数据准确性\n5. 供应商管理：建立稳定供应关系', 'document', '最佳实践', '库存管理,安全库存,ABC分类,盘点', 'inactive', NULL, 'text', 'pending', 2100246635, NOW(), NOW()),
-- 添加更多测试数据以验证分页查询功能
(8, 1, '人口普查方法论', '人口普查是全面了解人口状况的重要手段。普查内容包括人口数量、结构、分布等基本情况。我国每10年进行一次人口普查，在两次普查之间进行一次1%人口抽样调查。', 'document', '方法论', '人口普查,调查方法,统计方法', 'active', 'http://stats.gov.cn/census', 'text', 'completed', 2100246635, NOW(), NOW()),
(9, 1, '区域经济发展指标', '区域经济发展评价指标体系包括：经济总量、人均GDP、产业结构、投资效率、创新能力、生态环境等多个维度。不同地区应根据自身特点选择重点指标。', 'document', '指标体系', '区域经济,发展指标,评价体系', 'active', NULL, 'text', 'completed', 2100246635, NOW(), NOW()),
(10, 1, '数据质量控制规范', '数据质量控制是确保统计数据准确性的关键环节。包括数据采集、录入、审核、汇总等各个阶段的质量控制措施。建立数据质量责任制，确保数据真实可靠。', 'document', '质量控制', '数据质量,质量控制,规范标准', 'active', NULL, 'text', 'completed', 2100246635, NOW(), NOW()),
(11, 2, '销售漏斗分析方法', '销售漏斗是分析销售过程的重要工具。从潜在客户到成交客户，每个阶段都有转化率。通过分析各阶段转化率，找出销售瓶颈，优化销售流程。', 'document', '分析方法', '销售漏斗,转化率,流程优化', 'active', NULL, 'text', 'completed', 2100246635, NOW(), NOW()),
(12, 2, '客户细分策略', '客户细分是精准营销的基础。可以按照地理位置、人口统计、心理特征、行为特征等维度进行细分。针对不同细分市场制定差异化营销策略。', 'document', '营销策略', '客户细分,精准营销,市场策略', 'active', NULL, 'text', 'completed', 2100246635, NOW(), NOW()),
(13, 2, '销售预测模型', '销售预测是企业经营决策的重要依据。常用方法包括时间序列分析、回归分析、机器学习等。选择合适的预测模型需要考虑数据特点和业务需求。', 'document', '预测模型', '销售预测,时间序列,机器学习', 'active', 'https://sales.example.com/forecast', 'pdf', 'completed', 2100246635, NOW(), NOW()),
(14, 3, '成本核算方法', '成本核算是财务管理的基础工作。包括直接成本和间接成本的归集与分配。常用方法有品种法、分批法、分步法等。选择合适的成本核算方法对企业管理至关重要。', 'document', '成本管理', '成本核算,成本分配,核算方法', 'active', NULL, 'text', 'completed', 2100246635, NOW(), NOW()),
(15, 3, '财务比率分析', '财务比率分析是评价企业财务状况的重要方法。主要包括偿债能力比率、营运能力比率、盈利能力比率、发展能力比率等。通过比率分析可以全面了解企业经营状况。', 'document', '财务分析', '财务比率,偿债能力,盈利能力', 'active', NULL, 'text', 'completed', 2100246635, NOW(), NOW()),
(16, 3, '预算管理体系', '预算管理是企业内部控制的重要手段。包括预算编制、预算执行、预算控制、预算分析等环节。建立全面预算管理体系，提高资源配置效率。', 'document', '预算管理', '预算编制,预算控制,资源配置', 'active', NULL, 'text', 'completed', 2100246635, NOW(), NOW()),
(17, 4, '供应链风险管理', '供应链风险管理是保障供应链稳定运行的关键。主要风险包括供应中断、需求波动、价格波动、质量问题等。建立风险预警机制，制定应急预案。', 'document', '风险管理', '供应链,风险管理,应急预案', 'active', NULL, 'text', 'completed', 2100246635, NOW(), NOW()),
(18, 4, '仓储管理优化', '仓储管理优化可以提高物流效率，降低运营成本。包括仓库布局优化、货位管理、拣货路径优化、库存周转率提升等方面。运用信息技术提升仓储管理水平。', 'document', '仓储管理', '仓储优化,库存周转,信息化', 'active', NULL, 'text', 'completed', 2100246635, NOW(), NOW()),
(19, 4, '采购策略与技巧', '采购策略直接影响企业成本和供应链效率。包括供应商选择、采购批量决策、采购时机选择、价格谈判技巧等。建立战略采购体系，实现采购价值最大化。', 'document', '采购管理', '采购策略,供应商管理,成本控制', 'active', 'https://procurement.example.com/guide', 'pdf', 'completed', 2100246635, NOW(), NOW()),
(20, 1, '统计数据可视化指南', '数据可视化是数据分析的重要环节。选择合适的图表类型可以更直观地展示数据特征和趋势。常用图表包括柱状图、折线图、饼图、散点图等。', 'document', '可视化', '数据可视化,图表设计,数据展示', 'active', NULL, 'text', 'completed', 2100246635, NOW(), NOW()),
(21, 2, '客户流失预警模型', '客户流失预警是客户关系管理的重要内容。通过分析客户行为数据，建立流失预警模型，提前识别高风险客户，采取挽留措施。', 'faq', '客户管理', '客户流失,预警模型,客户挽留', 'active', NULL, 'text', 'completed', 2100246635, NOW(), NOW()),
(22, 3, '税务筹划基础知识', '税务筹划是在法律允许范围内，通过合理安排经营活动，降低税负的行为。需要深入了解税收政策，结合企业实际情况制定筹划方案。', 'faq', '税务管理', '税务筹划,税收政策,税负优化', 'active', NULL, 'text', 'completed', 2100246635, NOW(), NOW()),
(23, 4, '物流配送路径优化', '配送路径优化可以降低物流成本，提高配送效率。运用运筹学方法，考虑配送时间窗、车辆容量、道路状况等约束条件，制定最优配送方案。', 'faq', '物流配送', '路径优化,配送效率,成本控制', 'active', NULL, 'text', 'completed', 2100246635, NOW(), NOW());

-- 数据源示例数据
-- 示例数据源可以运行docker-compose-datasource.yml建立，或者手动修改为自己的数据源
INSERT IGNORE INTO `datasource` (`id`, `name`, `type`, `host`, `port`, `database_name`, `username`, `password`, `connection_url`, `status`, `test_status`, `description`, `creator_id`, `create_time`, `update_time`) VALUES 
(1, '生产环境MySQL数据库', 'mysql', 'mysql-data', 3306, 'product_db', 'root', 'root', 'jdbc:mysql://mysql-data:3306/product_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true', 'inactive', 'unknown', '生产环境主数据库，包含核心业务数据', 2100246635, NOW(), NOW()),
(2, '数据仓库PostgreSQL', 'postgresql', 'postgres-data', 5432, 'data_warehouse', 'postgres', 'postgres', 'jdbc:postgresql://postgres-data:5432/data_warehouse', 'inactive', 'unknown', '数据仓库，用于数据分析和报表生成', 2100246635, NOW(), NOW()),
(3, 'product_db', 'h2', null, null, 'product_db', 'root', 'root', 'jdbc:h2:mem:nl2sql_database;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=true;MODE=MySQL;DB_CLOSE_ON_EXIT=FALSE', 'inactive', 'unknown', 'h2测试数据库，包含核心业务数据', 2100246635, NOW(), NOW());

-- 智能体数据源关联示例数据
INSERT IGNORE INTO `agent_datasource` (`id`, `agent_id`, `datasource_id`, `is_active`, `create_time`, `update_time`) VALUES 
(1, 1, 2, 0, NOW(), NOW()),  -- 中国人口GDP数据智能体使用数据仓库
(2, 2, 3, 0, NOW(), NOW()),  -- 销售数据分析智能体使用生产环境数据库
(3, 3, 3, 0, NOW(), NOW()),  -- 财务报表智能体使用生产环境数据库
(4, 4, 3, 0, NOW(), NOW());  -- 库存管理智能体使用生产环境数据库
