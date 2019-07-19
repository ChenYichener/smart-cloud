package org.smartframework.cloud.code.generate.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.smartframework.cloud.code.generate.config.Config;
import org.smartframework.cloud.code.generate.dto.ColumnMetaDataDto;
import org.smartframework.cloud.code.generate.dto.TableMetaDataDto;
import org.smartframework.cloud.code.generate.dto.template.BaseMapperDto;
import org.smartframework.cloud.code.generate.dto.template.BaseRespBodyDto;
import org.smartframework.cloud.code.generate.dto.template.ClassCommentDto;
import org.smartframework.cloud.code.generate.dto.template.EntityAttributeDto;
import org.smartframework.cloud.code.generate.dto.template.EntityDto;

import lombok.experimental.UtilityClass;

/**
 * 模板dto工具类
 *
 * @author liyulin
 * @date 2019-07-15
 */
@UtilityClass
public class TemplateDtoUtil {

	/**
	 * 获取公共信息（如生成时间、作者等）
	 * 
	 * @param author
	 * @return
	 */
	public static ClassCommentDto getClassCommentDto(String author) {
		ClassCommentDto classComment = new ClassCommentDto();
		classComment.setCreateDate(new SimpleDateFormat(Config.CREATEDATE_FORMAT).format(new Date()));
		classComment.setAuthor(author);
		return classComment;
	}

	/**
	 * 获取生成Entity所需的参数信息
	 * 
	 * @param tableMetaData
	 * @param columnMetaDatas
	 * @param classComment
	 * @param mainClassPackage
	 * @return
	 */
	public static EntityDto getEntityDto(TableMetaDataDto tableMetaData, List<ColumnMetaDataDto> columnMetaDatas,
			ClassCommentDto classComment, String mainClassPackage) {
		EntityDto entityDto = new EntityDto();
		entityDto.setClassComment(classComment);
		entityDto.setTableName(tableMetaData.getName());
		entityDto.setTableComment(tableMetaData.getComment());
		entityDto.setPackageName(mainClassPackage + Config.ENTITY_PACKAGE_SUFFIX);
		entityDto.setClassName(JavaTypeUtil.getEntityName(tableMetaData.getName()));

		List<EntityAttributeDto> attributes = new ArrayList<>();
		entityDto.setAttributes(attributes);
		Set<String> importPackages = new HashSet<>();
		entityDto.setImportPackages(importPackages);
		for (ColumnMetaDataDto columnMetaData : columnMetaDatas) {
			EntityAttributeDto entityAttribute = new EntityAttributeDto();
			entityAttribute.setName(TableUtil.getAttibuteName(columnMetaData.getName()));
			entityAttribute.setColumnName(columnMetaData.getName());
			String comment = StringEscapeUtil.secapeComment(columnMetaData.getComment());
			entityAttribute.setComment(comment);
			entityAttribute
					.setJavaType(JavaTypeUtil.getByJdbcType(columnMetaData.getJdbcType(), columnMetaData.getLength()));
			String importPackage = JavaTypeUtil.getImportPackage(columnMetaData.getJdbcType());
			if (importPackage != null) {
				importPackages.add(importPackage);
			}

			attributes.add(entityAttribute);
		}
		return entityDto;
	}

	/**
	 * 获取生成BaseRespBody所需的参数信息
	 * 
	 * @param tableMetaData
	 * @param columnMetaDatas
	 * @param mainClassPackage
	 * @param importPackages
	 * @return
	 */
	public static BaseRespBodyDto getBaseRespBodyDto(TableMetaDataDto tableMetaData,
			List<ColumnMetaDataDto> columnMetaDatas, String mainClassPackage, Set<String> importPackages) {
		BaseRespBodyDto baseRespBodyDto = new BaseRespBodyDto();
		baseRespBodyDto.setTableComment(tableMetaData.getComment());
		baseRespBodyDto.setPackageName(getBaseRespBodyPackage(mainClassPackage));
		baseRespBodyDto.setClassName(JavaTypeUtil.getBaseRespBodyName(tableMetaData.getName()));
		baseRespBodyDto.setImportPackages(importPackages);

		List<EntityAttributeDto> attributes = new ArrayList<>();
		baseRespBodyDto.setAttributes(attributes);
		for (ColumnMetaDataDto columnMetaData : columnMetaDatas) {
			EntityAttributeDto entityAttribute = new EntityAttributeDto();
			String comment = StringEscapeUtil.secapeComment(columnMetaData.getComment());
			entityAttribute.setComment(comment);

			entityAttribute.setName(TableUtil.getAttibuteName(columnMetaData.getName()));
			entityAttribute
					.setJavaType(JavaTypeUtil.getByJdbcType(columnMetaData.getJdbcType(), columnMetaData.getLength()));

			attributes.add(entityAttribute);
		}
		return baseRespBodyDto;
	}

	/**
	 * 获取BaseRespBody包名
	 * 
	 * @param mainClassPackage
	 * @return
	 */
	private static String getBaseRespBodyPackage(String mainClassPackage) {
		int index = mainClassPackage.lastIndexOf('.');

		return mainClassPackage.subSequence(0, index) + ".rpc" + mainClassPackage.substring(index)
				+ Config.BASE_RESPBODY_PACKAGE_SUFFIX;
	}

	/**
	 * 获取生成BaesMapper所需的参数信息
	 * 
	 * @param tableMetaData
	 * @param entityDto
	 * @param baseRespBodyDto
	 * @param classComment
	 * @param mainClassPackage
	 * @return
	 */
	public static BaseMapperDto getBaseMapperDto(TableMetaDataDto tableMetaData, EntityDto entityDto,
			BaseRespBodyDto baseRespBodyDto, ClassCommentDto classComment, String mainClassPackage) {
		BaseMapperDto baseMapperDto = new BaseMapperDto();
		baseMapperDto.setClassComment(classComment);
		baseMapperDto.setTableComment(tableMetaData.getComment());
		baseMapperDto.setPackageName(mainClassPackage + Config.MAPPER_PACKAGE_SUFFIX);
		baseMapperDto.setClassName(JavaTypeUtil.getMapperName(tableMetaData.getName()));

		baseMapperDto.setEntityClassName(entityDto.getClassName());
		baseMapperDto.setImportEntityClass(entityDto.getPackageName() + "." + entityDto.getClassName());

		baseMapperDto.setBaseRespBodyClassName(baseRespBodyDto.getClassName());
		baseMapperDto
				.setImportBaseRespBodyClass(baseRespBodyDto.getPackageName() + "." + baseRespBodyDto.getClassName());
		return baseMapperDto;
	}

}