if($core)
  include Java

  import_java org.limewire.geocode.Geocoder
  import_java com.limegroup.gnutella.URN
  import_java com.limegroup.gnutella.metadata.MetaDataFactoryImpl
  import_java com.limegroup.gnutella.metadata.MetaDataFactory
  import_java org.limewire.io.GUID
  import_java org.limewire.core.api.library.LibraryManager
  import_java org.limewire.core.api.search.SearchManager
end

module Limewire
  def self.core=(c)
    @core = c
  end
  def self.core
    @core
  end

  def self.get_singleton(klass)
    $core.injector.get_instance(klass.java_class)
  end

  def self.uptime
    Limewire.core.get_statistics.uptime / 1000
  end

  def self.daily_uptime
    Limewire.core.get_statistics.calculate_daily_uptime
  end
      
  class Search
    def self.find(guid)
      self.new Limewire.get_singleton(SearchManager).getSearchByGuid(GUID.new(guid))
    end

    def self.query(query)
      self.new Limewire.get_singleton(SearchManager).createSearchFromQuery(query)
    end

    def initialize(search)
      @search = search
    end

    def results
      results = @search.getSearchResults
      results.map {|result| {:filename => result.fileName }}
    end

    def start
      @search.start
    end

    def query_string
      @search.getQueryString
    end

    def guid
      @search.getQueryGuid
    end

    def stop
      @search.stop
    end

    def restart
      @search.restart
    end
  end

  module Library
    def self.all_files
      file_list = Limewire.get_singleton(LibraryManager).library_managed_list.core_file_list
      file_list.map{ |file| Limewire::File.new(file) }.compact
    end

    def self.first(limit=1)
      self.all_files.first(limit)
    end

    def self.filter(&b)
      all_files.find_all(&b)
    end

    def self.find(type_or_sha1, options={})
      if(type == :all)
        files = all_files
      elsif(String === type)
        files = all_files.select{|f| f.sha1urn == type}
      end
      
      if options[:genres]
        all_files = all_files.select{|f| f.metadata.genre == options[:genres] }
      end

      limit = options[:limit].to_i || (type == :first) ? 1 : 40
      offset = options[:offset].to_i || 0
      
      all_files
    end

    def self.filter_by_name(regex)
      all_files.find_all{ |f| f.file_name =~ regex }
    end
    
    def self.categories
      Limewire.core.get_file_manager.get_managed_file_list.managed_categories rescue []
    end
    
  end

  class File
    def initialize(file)
      @file = file
      @metadata = Limewire.get_singleton(MetaDataFactory).parse(file.get_file) rescue nil
    end

    def metadata
      @metadata
    end

    def to_cloud
      {
        'duration' => metadata.length * 1000,
        'permalink' => metadata.title,
        'uri' => "/library/#{self.sHA1Urn}",
        'downloadable' => true,
        'genre' => metadata.genre,
        'title' => metadata.title.gsub(/\u0000/, ""),
        'id' => self.object_id,
        'streamable' => true,
        'stream_url' => "/library/#{self.sHA1Urn}",
        'description' => metadata.album.gsub(/\u0000/, ""),
        'permalink_url' => "/library/#{self.sHA1Urn}",
        'user' => {"username"=>metadata.artist.gsub(/\u0000/, "")},
        'sharing' => 'public',
        'purchase_url' => 'http://store.limewire.com'
      }
    end

    def method_missing(name, *args)
      if @file.respond_to?(name)
        @file.send(name, *args)
      else
        super
      end
    end
  end
end
