ActionController::Routing::Routes.draw do |map|

  map.file "/library/:sha1.mp3", :controller => 'library', :action => 'show'
  
  # Cloud Player
  map.resources :playlists, :path_prefix => "/cloud"
  map.tracks "/cloud/tracks.json", :controller => 'cloud', :action => 'tracks'
  map.new_search "/search/q/:query", :controller => 'search', :action => 'perform'
  map.search_control "/search/:guid/:query", :controller => 'search', :action => 'control'
  map.cloud "/cloud", :controller => 'cloud', :action => 'index'
  map.resources :downloads
  map.download '/download/:magnet', :controller => 'library', :action => 'download'
  map.resources :galleries, :collection => {:all => :get}

  map.assets '/assets/:plugin/*path', :controller => 'assets', :action => 'show'
  # The priority is based upon order of creation: first created -> highest priority.

  # Sample of regular route:
  #   map.connect 'products/:id', :controller => 'catalog', :action => 'view'
  # Keep in mind you can assign values other than :controller and :action

  # Sample of named route:
  #   map.purchase 'products/:id/purchase', :controller => 'catalog', :action => 'purchase'
  # This route can be invoked with purchase_url(:id => product.id)

  # Sample resource route (maps HTTP verbs to controller actions automatically):
  #   map.resources :products

  # Sample resource route with options:
  #   map.resources :products, :member => { :short => :get, :toggle => :post }, :collection => { :sold => :get }

  # Sample resource route with sub-resources:
  #   map.resources :products, :has_many => [ :comments, :sales ], :has_one => :seller
  
  # Sample resource route with more complex sub-resources
  #   map.resources :products do |products|
  #     products.resources :comments
  #     products.resources :sales, :collection => { :recent => :get }
  #   end

  # Sample resource route within a namespace:
  #   map.namespace :admin do |admin|
  #     # Directs /admin/products/* to Admin::ProductsController (app/controllers/admin/products_controller.rb)
  #     admin.resources :products
  #   end

  # You can have the root of your site routed with map.root -- just remember to delete public/index.html.
  # map.root :controller => "welcome"

  # See how all your routes lay out with "rake routes"

  # Install the default routes as the lowest priority.
  # Note: These default routes make all actions in every controller accessible via GET requests. You should
  # consider removing the them or commenting them out if you're using named routes and resources.
  # map.connect ':controller/:action/:id'
  # map.connect ':controller/:action/:id.:format'
  Dir.glob("#{RAILS_ROOT}/plugins/*/routes.rb").each do |routes_file|
    plugin_name = routes_file.split("/").reverse[1]
    map.with_options(:path_prefix => plugin_name) do |plugin_map|
      eval(File.open(routes_file).read)
    end
  end
end
