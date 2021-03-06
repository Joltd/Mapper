package com.evgenltd.mapper.core.bean;

import com.evgenltd.mapper.core.bean.envers.EnversBean;
import com.evgenltd.mapper.core.entity.impl.EntityFactory;
import com.evgenltd.mapper.core.util.Queries;
import javafx.scene.image.Image;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Project: Mapper
 * Author:  Evgeniy
 * Created: 13-06-2016 00:02
 */
@Component
public class DatabaseBeanMaintenance extends AbstractBean {

	@Autowired
	private EnversBean enversBean;

	@Transactional
	public void init()	{
		final Long version = getVersion();
		getLogger().info(String.format("Current database scheme version is [%s]",version));
		if(version < 11L)	{
			executeUpdate("/sql/scheme/init.sql",14L);
			executeBatchUpdate("/sql/scheme/defaults.sql");
			fillDefaultMarkerIconList();
		}else {
			switch(version.intValue())	{
				case 11:
					executeUpdate("/sql/scheme/version12.sql",12L);
				case 12:
					executeUpdate("/sql/scheme/version13.sql",13L);
				case 13:
					executeUpdate("/sql/scheme/version14.sql", 14L);
			}
		}
	}

	private Long getVersion()	{
		return (Long)getEntityManager()
				.createNativeQuery("pragma user_version")
				.getSingleResult();
	}

	private void updateVersion(@NotNull final Long version)	{
		getEntityManager()
				.createNativeQuery(String.format("pragma user_version = %s",version))
				.executeUpdate();
	}

	private void executeUpdate(@NotNull final String path, @NotNull final Long newVersion)	{

		getLogger().info(String.format("Update database to version %s", newVersion));

		executeBatchUpdate(path);

		updateVersion(newVersion);
	}

	private void fillDefaultMarkerIconList()	{
		final Image cave = new Image("/image/marker/cave.png");
		final Image claim = new Image("/image/marker/claim.png");
		final Image clay = new Image("/image/marker/clay.png");
		final Image none = new Image("/image/marker/none.png");
		final Image soil = new Image("/image/marker/soil.png");
		final Image village = new Image("/image/marker/village.png");
		final Image water = new Image("/image/marker/water.png");

		getEntityManager().persist(EntityFactory.createMarkerIcon("Cave", cave));
		getEntityManager().persist(EntityFactory.createMarkerIcon("Claim", claim));
		getEntityManager().persist(EntityFactory.createMarkerIcon("Clay", clay));
		getEntityManager().persist(EntityFactory.createMarkerIcon("None", none));
		getEntityManager().persist(EntityFactory.createMarkerIcon("Soil", soil));
		getEntityManager().persist(EntityFactory.createMarkerIcon("Village", village));
		getEntityManager().persist(EntityFactory.createMarkerIcon("Water", water));
	}

	// cleanup

	@Transactional
	public void processMaintenance()	{

		getEntityManager()
				.createNativeQuery(getQuery(Queries.MAINTENANCE_CLEANUP_DOUBLE_TILES_SQL))
				.executeUpdate();
		enversBean.cleanup();

	}

}
